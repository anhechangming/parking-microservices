package com.parking.fee.messaging;

import com.parking.fee.config.RabbitMQConfig;
import com.parking.fee.event.FeePaidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 费用事件发布者
 * 负责发布费用相关的异步事件到RabbitMQ
 *
 * @author Parking System
 */
@Component
public class FeeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FeeEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发布费用缴纳事件
     *
     * @param event 费用缴纳事件
     */
    public void publishFeePaidEvent(FeePaidEvent event) {
        try {
            // 设置事件ID（用于幂等性和消息追踪）
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }

            // 创建关联数据（用于确认回调）
            CorrelationData correlationData = new CorrelationData(event.getEventId());

            // 发送消息
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PARKING_EXCHANGE,
                    RabbitMQConfig.FEE_PAID_ROUTING_KEY,
                    event,
                    correlationData
            );

            log.info("已发布费用缴纳事件 - 事件ID: {}, 业主ID: {}, 费用ID: {}, 金额: {}",
                    event.getEventId(), event.getUserId(), event.getFeeId(), event.getPaymentAmount());

        } catch (Exception e) {
            log.error("发布费用缴纳事件失败 - 业主ID: {}, 费用ID: {}, 错误: {}",
                    event.getUserId(), event.getFeeId(), e.getMessage(), e);
            throw new RuntimeException("发布费用缴纳事件失败", e);
        }
    }
}
