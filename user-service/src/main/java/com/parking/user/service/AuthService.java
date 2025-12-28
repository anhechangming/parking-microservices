package com.parking.user.service;

import com.parking.user.common.JwtUtils;
import com.parking.user.entity.Admin;
import com.parking.user.entity.Owner;
import com.parking.user.mapper.AdminMapper;
import com.parking.user.mapper.OwnerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 *
 * @author Parking System
 */
@Service
public class AuthService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 管理员登录
     *
     * @param loginName 登录账号
     * @param password 密码
     * @return 登录结果（包含token和用户信息）
     */
    public Map<String, Object> adminLogin(String loginName, String password) {
        Admin admin = adminMapper.findByLoginName(loginName);

        if (admin == null) {
            throw new RuntimeException("账号不存在");
        }

        if (!"0".equals(admin.getStatus())) {
            throw new RuntimeException("账号已被停用");
        }

        // 临时测试：testadmin账号使用明文密码test123
        boolean passwordValid = false;
        if ("testadmin".equals(loginName) && "test123".equals(password)) {
            passwordValid = true;
            System.out.println("【临时测试】使用明文密码验证成功: testadmin/test123");
        } else {
            passwordValid = passwordEncoder.matches(password, admin.getPassword());
        }

        if (!passwordValid) {
            System.out.println("【密码验证失败】loginName=" + loginName + ", 输入密码=" + password + ", 数据库密码=" + admin.getPassword());
            throw new RuntimeException("密码错误");
        }

        // 生成Token（使用loginName作为subject，因为username可能为空）
        String token = jwtUtils.generateToken(admin.getUserId(), admin.getLoginName(), "admin");

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", admin.getUserId());
        result.put("username", admin.getUsername());
        result.put("roleType", "admin");

        return result;
    }

    /**
     * 业主登录
     *
     * @param loginName 登录账号
     * @param password 密码
     * @return 登录结果（包含token和用户信息）
     */
    public Map<String, Object> ownerLogin(String loginName, String password) {
        Owner owner = ownerMapper.findByLoginName(loginName);

        if (owner == null) {
            throw new RuntimeException("账号不存在");
        }

        if (!"0".equals(owner.getStatus())) {
            throw new RuntimeException("账号已被停用");
        }

        if (!passwordEncoder.matches(password, owner.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成Token（使用loginName作为subject，因为username可能为空）
        String token = jwtUtils.generateToken(owner.getUserId(), owner.getLoginName(), "owner");

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", owner.getUserId());
        result.put("username", owner.getUsername());
        result.put("roleType", "owner");

        return result;
    }
}
