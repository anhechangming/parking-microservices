package com.parking.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业主实体类
 *
 * @author Parking System
 */
@Data
public class Owner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业主ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 密码
     */
    private String password;

    /**
     * 业主姓名
     */
    private String username;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

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
