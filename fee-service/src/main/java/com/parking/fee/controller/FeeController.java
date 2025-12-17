package com.parking.fee.controller;

import com.parking.fee.common.PageResult;
import com.parking.fee.common.Result;
import com.parking.fee.entity.ParkingFee;
import com.parking.fee.service.ParkingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 费用管理控制器（管理员端）
 *
 * @author Parking System
 */
@RestController
@RequestMapping("/fee/admin")
public class FeeController {

    @Autowired
    private ParkingFeeService parkingFeeService;

    /**
     * 分页查询停车费列表
     */
    @GetMapping("/list")
    public Result<PageResult<ParkingFee>> getParkingFeePage(@RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(required = false) Long userId,
                                                             @RequestParam(required = false) String payStatus) {
        PageResult<ParkingFee> page = parkingFeeService.getParkingFeePage(pageNum, pageSize, userId, payStatus);
        return Result.success(page);
    }

    /**
     * 根据ID查询停车费
     */
    @GetMapping("/{parkFeeId}")
    public Result<ParkingFee> getParkingFeeById(@PathVariable Long parkFeeId) {
        ParkingFee parkingFee = parkingFeeService.getParkingFeeById(parkFeeId);
        if (parkingFee == null) {
            return Result.error("停车费记录不存在");
        }
        return Result.success(parkingFee);
    }

    /**
     * 新增停车费记录
     */
    @PostMapping
    public Result<Void> addParkingFee(@RequestBody ParkingFee parkingFee) {
        try {
            boolean success = parkingFeeService.addParkingFee(parkingFee);
            return success ? Result.success("新增成功", null) : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新停车费信息
     */
    @PutMapping("/{parkFeeId}")
    public Result<Void> updateParkingFee(@PathVariable Long parkFeeId, @RequestBody ParkingFee parkingFee) {
        parkingFee.setFeeId(parkFeeId);
        boolean success = parkingFeeService.updateParkingFee(parkingFee);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除停车费记录
     */
    @DeleteMapping("/{parkFeeId}")
    public Result<Void> deleteParkingFee(@PathVariable Long parkFeeId) {
        boolean success = parkingFeeService.deleteParkingFee(parkFeeId);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
