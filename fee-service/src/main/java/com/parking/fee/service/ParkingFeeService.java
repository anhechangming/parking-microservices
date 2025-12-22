package com.parking.fee.service;

import com.parking.fee.common.PageResult;
import com.parking.fee.entity.ParkingFee;
import com.parking.fee.mapper.ParkingFeeMapper;
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

    @Autowired
    private com.parking.fee.client.UserServiceClient userServiceClient;

    @Autowired
    private com.parking.fee.client.ParkingServiceClient parkingServiceClient;

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
        // 【跨服务调用】调用 user-service 获取用户信息（演示跨服务调用）
        try {
            com.parking.fee.common.Result<java.util.Map<String, Object>> result = userServiceClient.getOwnerById(userId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                // 可以根据用户类型（VIP/NORMAL）应用不同的业务逻辑
                String userType = (String) result.getData().get("userType");
                boolean isVip = "VIP".equalsIgnoreCase(userType);
                // 这里只是演示调用，实际业务逻辑可以根据VIP状态做不同处理
            }
        } catch (Exception e) {
            // 用户服务调用失败，记录日志但不影响查询停车费
            org.slf4j.LoggerFactory.getLogger(ParkingFeeService.class)
                .warn("调用user-service失败: {}", e.getMessage());
        }

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
        // 【跨服务调用1】验证用户是否存在
        com.parking.fee.common.Result<java.util.Map<String, Object>> userResult = userServiceClient.getOwnerById(userId);
        if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
            throw new RuntimeException("用户不存在，无法缴费");
        }

        // 查询费用记录
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

        // 【跨服务调用2 - 关键业务依赖】调用 parking-service 验证用户有有效的停车记录
        // 只有用户当前有停车位分配记录，才能缴纳停车费
        com.parking.fee.common.Result<java.util.Map<String, Object>> parkingResult =
            parkingServiceClient.getUserParkingRecord(userId);
        if (parkingResult == null || parkingResult.getCode() != 200 || parkingResult.getData() == null) {
            throw new RuntimeException("用户没有停车记录，无法缴费。请先分配车位。");
        }

        // 验证费用记录的车位ID与停车记录的车位ID一致
        java.util.Map<String, Object> parkingData = parkingResult.getData();
        Long recordParkId = parkingData.get("parkId") != null ?
            Long.valueOf(parkingData.get("parkId").toString()) : null;
        if (recordParkId == null || !recordParkId.equals(parkingFee.getParkId())) {
            throw new RuntimeException("费用记录与停车记录不匹配");
        }

        // 业务验证通过，执行缴费
        parkingFee.setPayParkStatus("1");
        parkingFee.setPayTime(new Date());
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
