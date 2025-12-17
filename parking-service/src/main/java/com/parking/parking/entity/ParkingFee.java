package com.parking.parking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 停车费实体类
 *
 * @author Parking System
 */
@Data
public class ParkingFee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 停车费ID
     */
    private Long parkFeeId;

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
    private String payParkMonth;

    /**
     * 缴费金额
     */
    private BigDecimal payParkMoney;

    /**
     * 缴费状态（0未缴 1已缴）
     */
    private String payParkStatus;

    /**
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payParkTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
