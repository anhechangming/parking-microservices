package com.parking.user.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos配置监听器
 * 监听配置变更事件
 */
@Slf4j
@Component
public class NacosConfigListener {

    @Value("${spring.cloud.nacos.config.server-addr:localhost:8848}")
    private String serverAddr;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${spring.cloud.nacos.config.namespace:dev}")
    private String namespace;

    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String group;

    @Value("${spring.cloud.nacos.config.file-extension:yaml}")
    private String fileExtension;

    @PostConstruct
    public void init() {
        try {
            // 构建配置dataId（服务名-环境.扩展名）
            String dataId = serviceName + "-dev." + fileExtension;

            log.info("初始化Nacos配置监听器: serverAddr={}, namespace={}, group={}, dataId={}",
                    serverAddr, namespace, group, dataId);

            // 创建配置服务
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);

            ConfigService configService = NacosFactory.createConfigService(properties);

            // 添加配置监听器
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("========== Nacos配置已更新 ==========");
                    log.info("DataId: {}", dataId);
                    log.info("Group: {}", group);
                    log.info("配置内容: \n{}", configInfo);
                    log.info("更新时间: {}", java.time.LocalDateTime.now());
                    log.info("======================================");
                }

                @Override
                public Executor getExecutor() {
                    return null;  // 使用默认线程池
                }
            });

            log.info("Nacos配置监听器启动成功");

        } catch (NacosException e) {
            log.error("初始化Nacos配置监听器失败", e);
        }
    }
}
