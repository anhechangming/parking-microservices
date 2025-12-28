package com.parking.parking.messaging;

import com.parking.parking.config.RabbitMQConfig;
import com.parking.parking.event.ParkingAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 停车事件发布者
 * 负责发布车位相关的异步事件到RabbitMQ
 *
 * @author Parking System
 */
@Component
public class ParkingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发布车位分配事件
     *
     * @param event 车位分配事件
     */
    public void publishParkingAssignedEvent(ParkingAssignedEvent event) {
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
                    RabbitMQConfig.PARKING_ASSIGNED_ROUTING_KEY,
                    event,
                    correlationData
            );

            log.info("已发布车位分配事件 - 事件ID: {}, 业主ID: {}, 车位ID: {}",
                    event.getEventId(), event.getUserId(), event.getParkId());

        } catch (Exception e) {
            log.error("发布车位分配事件失败 - 业主ID: {}, 车位ID: {}, 错误: {}",
                    event.getUserId(), event.getParkId(), e.getMessage(), e);
            throw new RuntimeException("发布车位分配事件失败", e);
        }
    }
}
