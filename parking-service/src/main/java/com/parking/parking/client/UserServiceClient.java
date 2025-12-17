package com.parking.parking.client;

import com.parking.parking.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 用户服务客户端
 * parking-service 调用 user-service 验证用户信息
 *
 * @author Parking System
 */
@Service
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service";

    /**
     * 根据用户ID获取业主信息（用于验证用户是否存在）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public Map<String, Object> getOwnerById(Long userId) {
        String url = USER_SERVICE_URL + "/user/owners/" + userId;
        log.info("调用user-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                log.info("成功获取用户信息: userId={}", userId);
                return result.getData();
            } else {
                log.error("获取用户信息失败: {}", result != null ? result.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("调用user-service失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("无法获取用户信息，用户可能不存在", e);
        }
    }

    /**
     * 验证用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    public boolean checkUserExists(Long userId) {
        try {
            Map<String, Object> owner = getOwnerById(userId);
            return owner != null;
        } catch (Exception e) {
            return false;
        }
    }
}
