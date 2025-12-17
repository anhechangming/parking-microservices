package com.parking.fee.client;

import com.parking.fee.common.Result;
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
 * 停车服务客户端
 * fee-service 调用 parking-service 获取停车记录信息
 *
 * @author Parking System
 */
@Service
public class ParkingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ParkingServiceClient.class);

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private static final String PARKING_SERVICE_URL = "http://parking-service";

    /**
     * 获取停车记录详情（用于计算费用）
     *
     * @param recordId 停车记录ID
     * @return 停车记录信息
     */
    public Map<String, Object> getParkingRecordById(Long recordId) {
        String url = PARKING_SERVICE_URL + "/parking/records/" + recordId;
        log.info("调用parking-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                log.info("成功获取停车记录: recordId={}", recordId);
                return result.getData();
            } else {
                log.error("获取停车记录失败: {}", result != null ? result.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("调用parking-service失败: recordId={}, error={}", recordId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户的停车记录
     *
     * @param userId 用户ID
     * @return 停车记录信息
     */
    public Map<String, Object> getUserParkingRecord(Long userId) {
        String url = PARKING_SERVICE_URL + "/parking/owner/record?userId=" + userId;
        log.info("【跨服务调用】调用parking-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                log.info("【跨服务调用成功】成功获取用户停车记录: userId={}", userId);
                return result.getData();
            } else {
                log.error("【跨服务调用失败】获取用户停车记录失败: {}", result != null ? result.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("【跨服务调用异常】调用parking-service失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("无法获取停车记录：" + e.getMessage());
        }
    }
}
