package com.parking.parking.client;

import com.parking.parking.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务Feign客户端
 * parking-service 通过OpenFeign调用 user-service 验证用户信息
 *
 * @author Parking System
 */
@FeignClient(
    name = "user-service",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * 根据用户ID获取业主信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/owners/{userId}")
    Result<Map<String, Object>> getOwnerById(@PathVariable("userId") Long userId);
}
