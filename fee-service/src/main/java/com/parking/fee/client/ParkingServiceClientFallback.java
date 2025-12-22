package com.parking.fee.client;

import com.parking.fee.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 停车服务Feign客户端降级实现
 * 当parking-service不可用时的降级逻辑
 *
 * @author Parking System
 */
@Component
public class ParkingServiceClientFallback implements ParkingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ParkingServiceClientFallback.class);

    @Override
    public Result<Map<String, Object>> getUserParkingRecord(Long userId) {
        log.error("【熔断降级】parking-service不可用，调用降级方法: userId={}", userId);
        return Result.error("停车服务暂时不可用，无法验证停车记录");
    }
}
