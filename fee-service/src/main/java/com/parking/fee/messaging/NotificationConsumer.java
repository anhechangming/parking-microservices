package com.parking.fee.messaging;

import com.parking.fee.config.RabbitMQConfig;
import com.parking.fee.event.FeePaidEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 通知消费者
 * 监听费用缴纳事件，发送缴费成功通知
 *
 * @author Parking System
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    /**
     * 监听费用缴纳事件，发送通知
     *
     * @param event 费用缴纳事件
     * @param message 原始消息
     * @param channel RabbitMQ通道
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleFeePaidEvent(FeePaidEvent event, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到费用缴纳事件 - 事件ID: {}, 业主ID: {}, 费用ID: {}, 金额: {}",
                    event.getEventId(), event.getUserId(), event.getFeeId(), event.getPaymentAmount());

            // 模拟发送缴费成功通知（实际应用中可以调用短信/邮件服务）
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String notificationMessage = String.format(
                    "【停车管理系统】缴费成功通知\n" +
                            "尊敬的业主（ID: %d）：\n" +
                            "您已成功缴纳%s月份的停车费，金额：¥%.2f\n" +
                            "缴费时间：%s\n" +
                            "感谢您的支持！",
                    event.getUserId(),
                    event.getPaymentMonth(),
                    event.getPaymentAmount(),
                    dateFormat.format(event.getPaymentTime())
            );

            log.info("发送缴费成功通知：\n{}", notificationMessage);

            // 这里可以集成短信/邮件服务
            // smsService.sendSMS(userPhone, notificationMessage);
            // emailService.sendEmail(userEmail, "缴费成功通知", notificationMessage);

            // 模拟发送成功
            log.info("缴费通知发送成功 - 业主ID: {}, 费用ID: {}", event.getUserId(), event.getFeeId());

            // 手动确认消息
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理费用缴纳事件失败 - 事件ID: {}, 错误: {}",
                    event.getEventId(), e.getMessage(), e);
            try {
                // 拒绝消息并重新入队（会触发重试，最终进入死信队列）
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("拒绝消息失败: {}", ioException.getMessage(), ioException);
            }
        }
    }
}
