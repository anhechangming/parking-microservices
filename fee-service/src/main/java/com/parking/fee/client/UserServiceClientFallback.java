package com.parking.fee.client;

import com.parking.fee.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户服务Feign客户端降级实现
 * 当user-service不可用时的降级逻辑
 *
 * @author Parking System
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public Result<Map<String, Object>> getOwnerById(Long userId) {
        log.error("【熔断降级】user-service不可用，调用降级方法: userId={}", userId);
        return Result.error("用户服务暂时不可用，请稍后重试");
    }
}
