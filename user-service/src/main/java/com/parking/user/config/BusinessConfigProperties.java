package com.parking.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 业务配置属性
 * 使用 @RefreshScope 支持配置动态刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "business")
public class BusinessConfigProperties {

    private Feature feature = new Feature();
    private Cache cache = new Cache();
    private Pagination pagination = new Pagination();

    @Data
    public static class Feature {
        private Boolean userRegistrationEnabled = true;  // 是否允许用户注册
        private Integer maxLoginAttempts = 5;  // 最大登录尝试次数
    }

    @Data
    public static class Cache {
        private Integer ttl = 3600;  // 缓存过期时间（秒）
    }

    @Data
    public static class Pagination {
        private Integer defaultPageSize = 10;  // 默认分页大小
        private Integer maxPageSize = 100;  // 最大分页大小
    }
}
