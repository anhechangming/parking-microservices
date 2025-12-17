package com.parking.parking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 车位实体类
 *
 * @author Parking System
 */
@Data
public class ParkingSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 车位ID
     */
    private Long parkId;

    /**
     * 车位编号
     */
    private String parkNum;

    /**
     * 车位类型（0:普通车位 1:充电车位 2:无障碍车位）
     */
    private String parkType;

    /**
     * 车位状态（0:空闲 1:已分配）
     */
    private String parkStatus;

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
