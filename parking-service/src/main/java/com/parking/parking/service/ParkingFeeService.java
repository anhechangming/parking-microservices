package com.parking.parking.service;

import com.parking.parking.common.PageResult;
import com.parking.parking.entity.ParkingFee;
import com.parking.parking.mapper.ParkingFeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 停车费服务
 *
 * @author Parking System
 */
@Service
public class ParkingFeeService {

    @Autowired
    private ParkingFeeMapper parkingFeeMapper;

    /**
     * 分页查询停车费列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 业主ID（可选）
     * @param payStatus 缴费状态（可选：0未缴 1已缴）
     * @return 停车费分页数据
     */
    public PageResult<ParkingFee> getParkingFeePage(int pageNum, int pageSize, Long userId, String payStatus) {
        int offset = (pageNum - 1) * pageSize;
        List<ParkingFee> records = parkingFeeMapper.findByPage(offset, pageSize, userId, payStatus);
        int total = parkingFeeMapper.countByConditions(userId, payStatus);
        return new PageResult<>(pageNum, pageSize, total, records);
    }

    /**
     * 查询业主的停车费记录
     *
     * @param userId 业主ID
     * @return 停车费列表
     */
    public List<ParkingFee> getOwnerParkingFees(Long userId) {
        return parkingFeeMapper.findByUserId(userId);
    }

    /**
     * 根据ID查询停车费
     *
     * @param parkFeeId 停车费ID
     * @return 停车费信息
     */
    public ParkingFee getParkingFeeById(Long parkFeeId) {
        return parkingFeeMapper.findById(parkFeeId);
    }

    /**
     * 新增停车费记录
     *
     * @param parkingFee 停车费信息
     * @return 是否成功
     */
    public boolean addParkingFee(ParkingFee parkingFee) {
        // 检查是否已存在该月份的停车费记录
        int count = parkingFeeMapper.countByUserIdAndParkIdAndMonth(
                parkingFee.getUserId(),
                parkingFee.getParkId(),
                parkingFee.getPayParkMonth()
        );
        if (count > 0) {
            throw new RuntimeException("该月份的停车费记录已存在");
        }

        parkingFee.setPayParkStatus("0"); // 默认未缴费
        return parkingFeeMapper.insert(parkingFee) > 0;
    }

    /**
     * 更新停车费信息
     *
     * @param parkingFee 停车费信息
     * @return 是否成功
     */
    public boolean updateParkingFee(ParkingFee parkingFee) {
        return parkingFeeMapper.update(parkingFee) > 0;
    }

    /**
     * 删除停车费记录
     *
     * @param parkFeeId 停车费ID
     * @return 是否成功
     */
    public boolean deleteParkingFee(Long parkFeeId) {
        return parkingFeeMapper.deleteById(parkFeeId) > 0;
    }

    /**
     * 业主缴纳停车费
     *
     * @param parkFeeId 停车费ID
     * @param userId 业主ID（用于验证）
     * @return 是否成功
     */
    public boolean payParkingFee(Long parkFeeId, Long userId) {
        ParkingFee parkingFee = parkingFeeMapper.findById(parkFeeId);
        if (parkingFee == null) {
            throw new RuntimeException("停车费记录不存在");
        }

        if (!parkingFee.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }

        if ("1".equals(parkingFee.getPayParkStatus())) {
            throw new RuntimeException("该停车费已缴纳");
        }

        parkingFee.setPayParkStatus("1");
        parkingFee.setPayParkTime(new Date());
        return parkingFeeMapper.update(parkingFee) > 0;
    }

    /**
     * 查询业主未缴费的停车费列表
     *
     * @param userId 业主ID
     * @return 未缴费列表
     */
    public List<ParkingFee> getUnpaidFees(Long userId) {
        return parkingFeeMapper.findUnpaidByUserId(userId);
    }

    /**
     * 批量生成停车费记录
     *
     * @param userId 业主ID
     * @param parkId 车位ID
     * @param month 月份（格式：2025-12）
     * @param amount 金额
     * @return 是否成功
     */
    public boolean generateParkingFee(Long userId, Long parkId, String month, BigDecimal amount) {
        ParkingFee parkingFee = new ParkingFee();
        parkingFee.setUserId(userId);
        parkingFee.setParkId(parkId);
        parkingFee.setPayParkMonth(month);
        parkingFee.setPayParkMoney(amount);
        parkingFee.setPayParkStatus("0");
        parkingFee.setRemark("系统生成");

        return addParkingFee(parkingFee);
    }
}
