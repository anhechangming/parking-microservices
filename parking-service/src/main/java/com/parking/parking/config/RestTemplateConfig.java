package com.parking.parking.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类（已废弃，使用OpenFeign替代）
 *
 * 阶段2：使用RestTemplate + @LoadBalanced实现服务间通信
 * 阶段3：迁移到OpenFeign实现声明式服务调用
 *
 * 此配置保留以备不时之需，当前推荐使用@FeignClient
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean（仅在不使用Feign时启用）
     * 当前系统已迁移至OpenFeign，此Bean已不再使用
     */
    // @Bean
    // @LoadBalanced
    // public RestTemplate restTemplate() {
    //     return new RestTemplate();
    // }
}
