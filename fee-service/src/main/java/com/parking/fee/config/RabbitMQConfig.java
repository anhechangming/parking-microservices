package com.parking.fee.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类 (阶段6 - 异步消息通信)
 * 配置交换机、队列、绑定关系和消息转换器
 *
 * @author Parking System
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 车位分配事件 ====================

    /**
     * 车位分配事件交换机（Topic类型）
     */
    public static final String PARKING_EXCHANGE = "parking.exchange";

    /**
     * 车位分配事件路由键
     */
    public static final String PARKING_ASSIGNED_ROUTING_KEY = "parking.assigned";

    /**
     * 费用服务队列（接收车位分配事件）
     */
    public static final String FEE_QUEUE = "fee.parking.assigned.queue";

    /**
     * 死信交换机
     */
    public static final String DEAD_LETTER_EXCHANGE = "parking.dlx.exchange";

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "parking.dlx.queue";

    // ==================== 费用缴纳事件 ====================

    /**
     * 费用缴纳事件路由键
     */
    public static final String FEE_PAID_ROUTING_KEY = "fee.paid";

    /**
     * 通知队列（接收费用缴纳事件，用于发送通知）
     */
    public static final String NOTIFICATION_QUEUE = "notification.fee.paid.queue";

    // ==================== 交换机配置 ====================

    /**
     * 创建主题交换机
     */
    @Bean
    public TopicExchange parkingExchange() {
        return ExchangeBuilder
                .topicExchange(PARKING_EXCHANGE)
                .durable(true)  // 持久化
                .build();
    }

    /**
     * 创建死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 队列配置 ====================

    /**
     * 创建费用服务队列（用于接收车位分配事件）
     */
    @Bean
    public Queue feeQueue() {
        return QueueBuilder
                .durable(FEE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)  // 配置死信交换机
                .withArgument("x-dead-letter-routing-key", "dlx")  // 死信路由键
                .build();
    }

    /**
     * 创建死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DEAD_LETTER_QUEUE)
                .build();
    }

    /**
     * 创建通知队列（用于接收费用缴纳事件）
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlx")
                .build();
    }

    // ==================== 绑定关系 ====================

    /**
     * 绑定费用队列到交换机
     */
    @Bean
    public Binding feeQueueBinding(Queue feeQueue, TopicExchange parkingExchange) {
        return BindingBuilder
                .bind(feeQueue)
                .to(parkingExchange)
                .with(PARKING_ASSIGNED_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("dlx");
    }

    /**
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationQueueBinding(Queue notificationQueue, TopicExchange parkingExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(parkingExchange)
                .with(FEE_PAID_ROUTING_KEY);
    }

    // ==================== 消息转换器 ====================

    /**
     * JSON消息转换器（用于序列化和反序列化消息）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate（用于发送消息）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // 配置消息发送确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功：" + correlationData);
            } else {
                System.err.println("消息发送失败：" + correlationData + "，原因：" + cause);
            }
        });

        // 配置消息返回回调（当消息无法路由时触发）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息路由失败：" + returned.getMessage()
                    + "，交换机：" + returned.getExchange()
                    + "，路由键：" + returned.getRoutingKey());
        });

        return rabbitTemplate;
    }

    /**
     * 配置监听器容器工厂（强制设置手动ACK模式）
     * 这个配置会覆盖application.yml中的设置
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // 强制设置为手动确认模式
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);

        System.out.println("【RabbitMQ配置】强制设置acknowledge-mode=MANUAL");

        return factory;
    }
}
