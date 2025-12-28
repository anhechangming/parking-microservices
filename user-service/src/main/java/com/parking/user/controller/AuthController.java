package com.parking.user.controller;

import com.parking.user.common.Result;
import com.parking.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    /**
     * 临时测试：生成BCrypt密码哈希
     * 仅用于测试，生产环境应删除
     *
     * @param password 原始密码
     * @return BCrypt哈希
     */
    @GetMapping("/test/bcrypt")
    public Result<String> generateBCryptHash(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("验证测试: " + encoder.matches(password, hash));
        return Result.success("BCrypt哈希生成成功", hash);
    }

    /**
     * 临时测试：快速创建管理员账号
     * 仅用于测试，生产环境应删除
     */
    @PostMapping("/test/create-admin")
    public Result<String> createTestAdmin(@RequestParam String loginName,
                                           @RequestParam String password,
                                           @RequestParam String username) {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hashedPassword = encoder.encode(password);

            // 使用原生JDBC插入，避免依赖Mapper
            String sql = "INSERT INTO sys_user (login_name, password, username, status) VALUES ('"
                + loginName + "', '" + hashedPassword + "', '" + username + "', '0')";

            System.out.println("【创建管理员】SQL: " + sql);
            System.out.println("【创建管理员】loginName=" + loginName + ", password=" + password + ", hashedPassword=" + hashedPassword);

            return Result.success("管理员创建SQL已生成，请手动执行", sql);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }
}
