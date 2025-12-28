package com.parking.parking.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 车位分配事件
 * 当车位被分配给业主时发布此事件，用于通知费用服务自动创建费用记录
 *
 * @author Parking System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingAssignedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（用于幂等性）
     */
    private String eventId;

    /**
     * 业主车位关联ID
     */
    private Long ownerParkingId;

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
    private String carNumber;

    /**
     * 入场时间
     */
    private Date entryTime;

    /**
     * 事件发生时间
     */
    private Date eventTime;
}
