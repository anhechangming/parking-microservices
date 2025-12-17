package com.parking.fee;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.parking.fee.mapper")
public class FeeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeeServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("费用服务启动成功！");
        System.out.println("========================================");
    }
}
