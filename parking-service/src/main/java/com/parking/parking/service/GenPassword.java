package com.parking.parking.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + encoded);

        // 验证
        boolean matches = encoder.matches(password, encoded);
        System.out.println("验证结果: " + matches);
    }
}
