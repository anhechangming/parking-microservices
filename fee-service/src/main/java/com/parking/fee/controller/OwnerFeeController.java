package com.parking.fee.controller;

import com.parking.fee.common.Result;
import com.parking.fee.entity.ParkingFee;
import com.parking.fee.service.ParkingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 费用控制器（业主端）
 *
 * @author Parking System
 */
@RestController
@RequestMapping("/fee/owner")
public class OwnerFeeController {

    @Autowired
    private ParkingFeeService parkingFeeService;

    /**
     * 查看我的停车费记录
     *
     * @param userId 业主ID（从Token获取）
     * @return 停车费列表
     */
    @GetMapping("/my-fees")
    public Result<List<ParkingFee>> getMyParkingFees(@RequestParam Long userId) {
        List<ParkingFee> fees = parkingFeeService.getOwnerParkingFees(userId);
        return Result.success(fees);
    }

    /**
     * 查看未缴费的停车费列表
     *
     * @param userId 业主ID（从Token获取）
     * @return 未缴费列表
     */
    @GetMapping("/unpaid")
    public Result<List<ParkingFee>> getUnpaidFees(@RequestParam Long userId) {
        List<ParkingFee> fees = parkingFeeService.getUnpaidFees(userId);
        return Result.success(fees);
    }

    /**
     * 在线缴纳停车费
     *
     * @param parkFeeId 停车费ID
     * @param userId 业主ID（从Token获取）
     * @return 缴费结果
     */
    @PostMapping("/pay")
    public Result<Void> payParkingFee(@RequestParam Long parkFeeId,
                                       @RequestParam Long userId) {
        try {
            boolean success = parkingFeeService.payParkingFee(parkFeeId, userId);
            return success ? Result.success("缴费成功", null) : Result.error("缴费失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看停车费详情
     *
     * @param parkFeeId 停车费ID
     * @return 停车费详情
     */
    @GetMapping("/{parkFeeId}")
    public Result<ParkingFee> getParkingFeeDetail(@PathVariable Long parkFeeId) {
        ParkingFee parkingFee = parkingFeeService.getParkingFeeById(parkFeeId);
        if (parkingFee == null) {
            return Result.error("停车费记录不存在");
        }
        return Result.success(parkingFee);
    }
}
