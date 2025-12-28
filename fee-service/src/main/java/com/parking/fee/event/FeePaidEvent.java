package com.parking.fee.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 费用缴纳事件
 * 当业主成功缴纳停车费时发布此事件，用于通知系统发送缴费通知、更新统计等
 *
 * @author Parking System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeePaidEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 费用记录ID
     */
    private Long feeId;

    /**
     * 业主ID
     */
    private Long userId;

    /**
     * 车位ID
     */
    private Long parkId;

    /**
     * 缴费月份（格式：2025-01）
     */
    private String paymentMonth;

    /**
     * 缴费金额
     */
    private BigDecimal paymentAmount;

    /**
     * 缴费时间
     */
    private Date paymentTime;

    /**
     * 事件发生时间
     */
    private Date eventTime;
}
