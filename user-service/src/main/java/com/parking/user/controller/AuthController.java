package com.parking.user.controller;

import com.parking.user.common.Result;
import com.parking.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 - 统一登录接口
 *
 * @author Parking System
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 管理员登录
     *
     * @param loginName 登录账号
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestParam String loginName,
                                                   @RequestParam String password) {
        try {
            Map<String, Object> result = authService.adminLogin(loginName, password);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            e.printStackTrace();  // 打印完整异常堆栈
            return Result.error(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    /**
     * 业主登录
     *
     * @param loginName 登录账号
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/owner/login")
    public Result<Map<String, Object>> ownerLogin(@RequestParam String loginName,
                                                   @RequestParam String password) {
        try {
            Map<String, Object> result = authService.ownerLogin(loginName, password);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            e.printStackTrace();  // 打印完整异常堆栈
            return Result.error(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    /**
     * 退出登录（前端删除Token即可）
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
}
