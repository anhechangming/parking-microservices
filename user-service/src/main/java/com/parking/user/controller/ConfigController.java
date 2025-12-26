package com.parking.user.controller;

import com.parking.user.config.BusinessConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理Controller
 * 用于查看当前配置值，测试动态配置刷新
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RefreshScope  // 支持配置动态刷新
public class ConfigController {

    @Autowired
    private BusinessConfigProperties businessConfig;

    @Value("${business.feature.user-registration-enabled:true}")
    private Boolean userRegistrationEnabled;

    @Value("${business.pagination.max-page-size:100}")
    private Integer maxPageSize;

    /**
     * 获取当前配置
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentConfig() {
        log.info("查询当前配置");

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("serviceName", "user-service");

        // 使用 @Value 注解获取的配置
        Map<String, Object> valueConfig = new HashMap<>();
        valueConfig.put("userRegistrationEnabled", userRegistrationEnabled);
        valueConfig.put("maxPageSize", maxPageSize);
        result.put("configByValue", valueConfig);

        // 使用 ConfigurationProperties 获取的配置
        Map<String, Object> propertiesConfig = new HashMap<>();
        propertiesConfig.put("userRegistrationEnabled", businessConfig.getFeature().getUserRegistrationEnabled());
        propertiesConfig.put("maxLoginAttempts", businessConfig.getFeature().getMaxLoginAttempts());
        propertiesConfig.put("cacheTtl", businessConfig.getCache().getTtl());
        propertiesConfig.put("defaultPageSize", businessConfig.getPagination().getDefaultPageSize());
        propertiesConfig.put("maxPageSize", businessConfig.getPagination().getMaxPageSize());
        result.put("configByProperties", propertiesConfig);

        return result;
    }

    /**
     * 测试业务逻辑 - 根据配置决定是否允许注册
     */
    @GetMapping("/test-registration")
    public Map<String, Object> testRegistration() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (businessConfig.getFeature().getUserRegistrationEnabled()) {
            result.put("status", "success");
            result.put("message", "用户注册功能已开启");
            log.info("用户注册功能已开启");
        } else {
            result.put("status", "disabled");
            result.put("message", "用户注册功能已关闭，请联系管理员");
            log.warn("用户注册功能已关闭");
        }

        return result;
    }
}
