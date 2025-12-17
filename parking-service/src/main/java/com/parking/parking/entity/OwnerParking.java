package com.parking.parking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业主车位关联实体类
 *
 * @author Parking System
 */
@Data
public class OwnerParking implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业主ID
     */
    private Long userId;

    /**
     * 车位ID
     */
    private Long parkId;

    /**
     * 车牌号
     */
    private String carNum;

    /**
     * 入场时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime;

    /**
     * 出场时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date exitTime;

    /**
     * 停车天数
     */
    private Integer parkingDays;

    /**
     * 停车费用
     */
    private java.math.BigDecimal parkingFee;

    /**
     * 缴费状态（0未缴费 1已缴费）
     */
    private String paymentStatus;

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
