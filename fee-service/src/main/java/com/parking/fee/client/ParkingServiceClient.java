package com.parking.fee.client;

import com.parking.fee.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 停车服务Feign客户端
 * fee-service 通过OpenFeign调用 parking-service 获取停车记录信息
 *
 * @author Parking System
 */
@FeignClient(
    name = "parking-service",
    fallback = ParkingServiceClientFallback.class
)
public interface ParkingServiceClient {

    /**
     * 【供跨服务调用】根据用户ID获取停车记录
     * 用于fee-service在缴费前验证用户是否有有效停车记录
     *
     * @param userId 用户ID
     * @return 停车记录信息
     */
    @GetMapping("/parking/owner/record")
    Result<Map<String, Object>> getUserParkingRecord(@RequestParam("userId") Long userId);
}
