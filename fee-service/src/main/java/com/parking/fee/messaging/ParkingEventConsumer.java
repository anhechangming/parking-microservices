package com.parking.fee.messaging;

import com.parking.fee.config.RabbitMQConfig;
import com.parking.fee.entity.ParkingFee;
import com.parking.fee.event.ParkingAssignedEvent;
import com.parking.fee.mapper.ParkingFeeMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 停车事件消费者
 * 监听车位分配事件，自动创建费用记录
 *
 * @author Parking System
 */
@Component
public class ParkingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ParkingEventConsumer.class);

    @Autowired
    private ParkingFeeMapper parkingFeeMapper;

    /**
     * 监听车位分配事件，自动创建费用记录
     *
     * @param event 车位分配事件
     * @param message 原始消息
     * @param channel RabbitMQ通道
     */
    @RabbitListener(queues = RabbitMQConfig.FEE_QUEUE)
    public void handleParkingAssignedEvent(ParkingAssignedEvent event, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到车位分配事件 - 事件ID: {}, 业主ID: {}, 车位ID: {}",
                    event.getEventId(), event.getUserId(), event.getParkId());

            // 获取当前月份（格式：2025-01）
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            String currentMonth = monthFormat.format(event.getEntryTime());

            // 【幂等性检查】检查是否已经创建过该月的费用记录
            int existingCount = parkingFeeMapper.countByUserIdAndParkIdAndMonth(
                    event.getUserId(),
                    event.getParkId(),
                    currentMonth
            );

            if (existingCount > 0) {
                log.warn("费用记录已存在，跳过创建 - 业主ID: {}, 车位ID: {}, 月份: {}",
                        event.getUserId(), event.getParkId(), currentMonth);
                // 消息已处理（幂等性），确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 创建费用记录
            ParkingFee parkingFee = new ParkingFee();
            parkingFee.setUserId(event.getUserId());
            parkingFee.setParkId(event.getParkId());
            parkingFee.setPayParkMonth(currentMonth);
            parkingFee.setPayParkMoney(new BigDecimal("300.00"));  // 默认月费300元
            parkingFee.setPayParkStatus("0");  // 未缴费
            // 注：数据库表没有remark字段，使用日志记录事件ID

            int result = parkingFeeMapper.insert(parkingFee);

            if (result > 0) {
                log.info("成功创建费用记录 - 费用ID: {}, 业主ID: {}, 车位ID: {}, 月份: {}, 金额: {}",
                        parkingFee.getFeeId(), event.getUserId(), event.getParkId(),
                        currentMonth, parkingFee.getPayParkMoney());

                // 手动确认消息
                channel.basicAck(deliveryTag, false);
            } else {
                log.error("创建费用记录失败 - 业主ID: {}, 车位ID: {}", event.getUserId(), event.getParkId());
                // 拒绝消息，不重新入队（会进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            }

        } catch (Exception e) {
            log.error("处理车位分配事件失败 - 事件ID: {}, 错误: {}",
                    event.getEventId(), e.getMessage(), e);
            try {
                // 拒绝消息，不重新入队（让Spring Retry处理重试，最终进入死信队列）
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("拒绝消息失败: {}", ioException.getMessage(), ioException);
            }
        }
    }
}
