package com.parking.parking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.parking.parking.mapper")
public class ParkingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("停车业务服务启动成功！");
        System.out.println("========================================");
    }
}
