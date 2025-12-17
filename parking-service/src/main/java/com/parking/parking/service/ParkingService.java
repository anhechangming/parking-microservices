package com.parking.parking.service;

import com.parking.parking.common.PageResult;
import com.parking.parking.entity.OwnerParking;
import com.parking.parking.entity.ParkingSpace;
import com.parking.parking.mapper.OwnerParkingMapper;
import com.parking.parking.mapper.ParkingSpaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 车位服务
 *
 * @author Parking System
 */
@Service
public class ParkingService {

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Autowired
    private OwnerParkingMapper ownerParkingMapper;

    @Autowired
    private com.parking.parking.client.UserServiceClient userServiceClient;

    /**
     * 分页查询车位列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词（车位编号或名称）
     * @param status 车位状态（0空闲 1已分配）
     * @return 车位分页数据
     */
    public PageResult<ParkingSpace> getParkingPage(int pageNum, int pageSize, String keyword, String status) {
        int offset = (pageNum - 1) * pageSize;
        List<ParkingSpace> records = parkingSpaceMapper.findByPage(offset, pageSize, keyword, status);
        int total = parkingSpaceMapper.countByKeyword(keyword, status);
        return new PageResult<>(pageNum, pageSize, total, records);
    }

    /**
     * 查询所有空闲车位
     *
     * @return 空闲车位列表
     */
    public List<ParkingSpace> getAvailableParkings() {
        return parkingSpaceMapper.findAvailable();
    }

    /**
     * 根据ID查询车位
     *
     * @param parkId 车位ID
     * @return 车位信息
     */
    public ParkingSpace getParkingById(Long parkId) {
        return parkingSpaceMapper.findById(parkId);
    }

    /**
     * 新增车位
     *
     * @param parkingSpace 车位信息
     * @return 是否成功
     */
    public boolean addParkingSpace(ParkingSpace parkingSpace) {
        // 检查车位编号是否重复
        int count = parkingSpaceMapper.countByParkNum(parkingSpace.getParkNum());
        if (count > 0) {
            throw new RuntimeException("车位编号已存在");
        }

        parkingSpace.setParkStatus("0"); // 默认空闲
        return parkingSpaceMapper.insert(parkingSpace) > 0;
    }

    /**
     * 更新车位信息
     *
     * @param parkingSpace 车位信息
     * @return 是否成功
     */
    public boolean updateParkingSpace(ParkingSpace parkingSpace) {
        return parkingSpaceMapper.update(parkingSpace) > 0;
    }

    /**
     * 删除车位
     *
     * @param parkId 车位ID
     * @return 是否成功
     */
    public boolean deleteParkingSpace(Long parkId) {
        // 检查是否已分配
        ParkingSpace parking = parkingSpaceMapper.findById(parkId);
        if (parking != null && "1".equals(parking.getParkStatus())) {
            throw new RuntimeException("车位已分配，无法删除");
        }

        return parkingSpaceMapper.deleteById(parkId) > 0;
    }

    /**
     * 分配车位给业主
     *
     * @param userId 业主ID
     * @param parkId 车位ID
     * @param carNumber 车牌号
     * @return 是否成功
     */
    @Transactional
    public boolean assignParkingToOwner(Long userId, Long parkId, String carNumber) {
        // 【跨服务调用】验证用户是否存在
        if (!userServiceClient.checkUserExists(userId)) {
            throw new RuntimeException("用户不存在，无法分配车位");
        }

        // 检查车位是否存在且空闲
        ParkingSpace parking = parkingSpaceMapper.findById(parkId);
        if (parking == null) {
            throw new RuntimeException("车位不存在");
        }
        if ("1".equals(parking.getParkStatus())) {
            throw new RuntimeException("车位已被分配");
        }

        // 检查业主是否已有车位
        int count = ownerParkingMapper.countByUserIdAndActive(userId);
        if (count > 0) {
            throw new RuntimeException("该业主已有车位，请先退位");
        }

        // 创建业主车位关联
        OwnerParking ownerParking = new OwnerParking();
        ownerParking.setUserId(userId);
        ownerParking.setParkId(parkId);
        ownerParking.setCarNum(carNumber);
        ownerParking.setEntryTime(new Date());
        ownerParking.setPaymentStatus("1");

        boolean insertSuccess = ownerParkingMapper.insert(ownerParking) > 0;

        if (insertSuccess) {
            // 更新车位状态为已分配
            parking.setParkStatus("1");
            parkingSpaceMapper.update(parking);
        }

        return insertSuccess;
    }

    /**
     * 业主退车位
     *
     * @param userId 业主ID
     * @return 是否成功
     */
    @Transactional
    public boolean returnParking(Long userId) {
        // 查询业主当前的车位
        OwnerParking ownerParking = ownerParkingMapper.findByUserIdAndActive(userId);

        if (ownerParking == null) {
            throw new RuntimeException("该业主没有分配车位");
        }

        // 更新关联状态为已退位
        ownerParking.setPaymentStatus("0");
        boolean updateSuccess = ownerParkingMapper.update(ownerParking) > 0;

        if (updateSuccess) {
            // 更新车位状态为空闲
            ParkingSpace parking = parkingSpaceMapper.findById(ownerParking.getParkId());
            if (parking != null) {
                parking.setParkStatus("0");
                parkingSpaceMapper.update(parking);
            }
        }

        return updateSuccess;
    }

    /**
     * 查询业主的车位信息
     *
     * @param userId 业主ID
     * @return 业主车位关联信息
     */
    public OwnerParking getOwnerParking(Long userId) {
        return ownerParkingMapper.findByUserIdAndActive(userId);
    }
}
