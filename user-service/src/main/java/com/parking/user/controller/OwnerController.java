package com.parking.user.controller;

import com.parking.user.common.PageResult;
import com.parking.user.common.Result;
import com.parking.user.entity.Owner;
import com.parking.user.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业主管理控制器
 *
 * @author Parking System
 */
@RestController
@RequestMapping("/user/owners")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    /**
     * 分页查询业主列表
     */
    @GetMapping
    public Result<PageResult<Owner>> getOwnerPage(@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false) String keyword) {
        PageResult<Owner> page = ownerService.getOwnerPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 查询所有业主（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<Owner>> getAllOwners() {
        List<Owner> owners = ownerService.getAllOwners();
        return Result.success(owners);
    }

    /**
     * 根据ID查询业主
     */
    @GetMapping("/{userId}")
    public Result<Owner> getOwnerById(@PathVariable Long userId) {
        Owner owner = ownerService.getOwnerById(userId);
        if (owner == null) {
            return Result.error("业主不存在");
        }
        return Result.success(owner);
    }

    /**
     * 新增业主
     */
    @PostMapping
    public Result<Void> addOwner(@RequestBody Owner owner) {
        try {
            boolean success = ownerService.addOwner(owner);
            return success ? Result.success("新增成功", null) : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新业主信息
     */
    @PutMapping("/{userId}")
    public Result<Void> updateOwner(@PathVariable Long userId, @RequestBody Owner owner) {
        owner.setUserId(userId);
        boolean success = ownerService.updateOwner(owner);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 删除业主
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteOwner(@PathVariable Long userId) {
        try {
            boolean success = ownerService.deleteOwner(userId);
            return success ? Result.success("删除成功", null) : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
