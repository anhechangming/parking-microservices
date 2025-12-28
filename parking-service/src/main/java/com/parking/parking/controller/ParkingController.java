package com.parking.parking.controller;

import com.parking.parking.common.PageResult;
import com.parking.parking.common.Result;
import com.parking.parking.entity.ParkingFee;
import com.parking.parking.entity.ParkingSpace;
import com.parking.parking.service.ParkingFeeService;
import com.parking.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 停车管理控制器（管理员端）
 *
 * @author Parking System
 */
@RestController
@RequestMapping("/parking/admin")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private ParkingFeeService parkingFeeService;

    // ==================== 车位管理 ====================

    /**
     * 分页查询车位列表
     */
    @GetMapping("/parkings")
    public Result<PageResult<ParkingSpace>> getParkingPage(@RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) String status) {
        PageResult<ParkingSpace> page = parkingService.getParkingPage(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    /**
     * 查询所有空闲车位（用于分配）
     */
    @GetMapping("/parkings/available")
    public Result<List<ParkingSpace>> getAvailableParkings() {
        List<ParkingSpace> parkings = parkingService.getAvailableParkings();
        return Result.success(parkings);
    }

    /**
     * 根据ID查询车位
     */
    @GetMapping("/parkings/{parkId}")
    public Result<ParkingSpace> getParkingById(@PathVariable Long parkId) {
        ParkingSpace parking = parkingService.getParkingById(parkId);
        if (parking == null) {
            return Result.error("车位不存在");
        }
        return Result.success(parking);
    }

    /**
     * 新增车位
     */
    @PostMapping("/parkings")
    public Result<Void> addParkingSpace(@RequestBody ParkingSpace parkingSpace) {
        try {
            boolean success = parkingService.addParkingSpace(parkingSpace);
            return success ? Result.success("新增成功", null) : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新车位信息
     */
    @PutMapping("/parkings/{parkId}")
    public Result<Void> updateParkingSpace(@PathVariable Long parkId, @RequestBody ParkingSpace parkingSpace) {
        parkingSpace.setParkId(parkId);
        boolean success = parkingService.updateParkingSpace(parkingSpace);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除车位
     */
    @DeleteMapping("/parkings/{parkId}")
    public Result<Void> deleteParkingSpace(@PathVariable Long parkId) {
        try {
            boolean success = parkingService.deleteParkingSpace(parkId);
            return success ? Result.success("删除成功", null) : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分配车位给业主
     */
    @PostMapping("/parkings/assign")
    public Result<Void> assignParking(@RequestParam Long userId,
                                       @RequestParam Long parkId,
                                       @RequestParam(required = false) String carNumber) {
        try {
            boolean success = parkingService.assignParkingToOwner(userId, parkId, carNumber);
            return success ? Result.success("分配成功", null) : Result.error("分配失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 业主退车位（支持通过userId或parkId退还）
     */
    @PostMapping("/parkings/return")
    public Result<Void> returnParking(@RequestParam(required = false) Long userId,
                                       @RequestParam(required = false) Long parkId) {
        try {
            // 优先使用parkId（管理员界面），如果没有则使用userId（业主界面）
            if (parkId != null) {
                boolean success = parkingService.returnParkingByParkId(parkId);
                return success ? Result.success("退位成功", null) : Result.error("退位失败");
            } else if (userId != null) {
                boolean success = parkingService.returnParking(userId);
                return success ? Result.success("退位成功", null) : Result.error("退位失败");
            } else {
                return Result.error("请提供userId或parkId参数");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 停车费管理 ====================

    /**
     * 分页查询停车费列表
     */
    @GetMapping("/parking-fees")
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
    @GetMapping("/parking-fees/{parkFeeId}")
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
    @PostMapping("/parking-fees")
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
    @PutMapping("/parking-fees/{parkFeeId}")
    public Result<Void> updateParkingFee(@PathVariable Long parkFeeId, @RequestBody ParkingFee parkingFee) {
        parkingFee.setParkFeeId(parkFeeId);
        boolean success = parkingFeeService.updateParkingFee(parkingFee);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除停车费记录
     */
    @DeleteMapping("/parking-fees/{parkFeeId}")
    public Result<Void> deleteParkingFee(@PathVariable Long parkFeeId) {
        boolean success = parkingFeeService.deleteParkingFee(parkFeeId);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}
