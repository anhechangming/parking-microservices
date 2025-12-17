package com.parking.user.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 用于服务间通信（阶段2：服务拆分与注册发现）
 *
 * @LoadBalanced注解：启用客户端负载均衡
 * 通过服务名调用其他微服务，由Nacos提供服务发现能力
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean
     * @LoadBalanced 注解使RestTemplate具备负载均衡能力
     *
     * 使用示例：
     * restTemplate.getForObject("http://parking-service/api/parking/1", ParkingSpace.class)
     */
    @Bean
    @LoadBalanced  // 启用客户端负载均衡（Spring Cloud LoadBalancer）
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
