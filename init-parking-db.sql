-- 停车业务服务数据库初始化
USE parking_business_db;

-- 停车位表
CREATE TABLE IF NOT EXISTS parking_space (
    park_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '停车位ID',
    park_num VARCHAR(20) NOT NULL UNIQUE COMMENT '停车位编号',
    park_type CHAR(1) DEFAULT '0' COMMENT '停车位类型（0地面 1地下）',
    park_status CHAR(1) DEFAULT '0' COMMENT '状态（0空闲 1占用）',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_park_num (park_num),
    INDEX idx_park_status (park_status),
    INDEX idx_park_type (park_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车位表';

-- 停车记录表
CREATE TABLE IF NOT EXISTS owner_parking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '停车记录ID',
    user_id BIGINT NOT NULL COMMENT '业主ID',
    park_id BIGINT NOT NULL COMMENT '停车位ID',
    car_num VARCHAR(20) NOT NULL COMMENT '车牌号',
    entry_time DATETIME NOT NULL COMMENT '入场时间',
    exit_time DATETIME COMMENT '出场时间',
    parking_days INT DEFAULT 0 COMMENT '停车天数',
    parking_fee DECIMAL(10,2) COMMENT '停车费用',
    payment_status CHAR(1) DEFAULT '0' COMMENT '缴费状态（0未缴费 1已缴费）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_park_id (park_id),
    INDEX idx_car_num (car_num),
    INDEX idx_entry_time (entry_time),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车记录表';

-- 清空现有数据
TRUNCATE TABLE owner_parking;
TRUNCATE TABLE parking_space;

-- 初始化停车位数据
INSERT INTO parking_space (park_num, park_type, park_status, remark)
VALUES
    ('A-001', '0', '0', '地面停车位'),
    ('A-002', '0', '0', '地面停车位'),
    ('A-003', '0', '0', '地面停车位'),
    ('B-001', '1', '0', '地下停车位'),
    ('B-002', '1', '0', '地下停车位'),
    ('B-003', '1', '0', '地下停车位');

-- 初始化测试停车记录（为跨服务调用测试准备数据）
INSERT INTO owner_parking (user_id, park_id, car_num, entry_time, exit_time, parking_days, parking_fee, payment_status)
VALUES
    (1, 1, '京A12345', '2025-12-01 08:00:00', '2025-12-10 18:00:00', 9, 900.00, '0'),
    (2, 2, '京B67890', '2025-12-05 09:00:00', '2025-12-15 17:00:00', 10, 1000.00, '0'),
    (3, 3, '京C11111', '2025-12-10 10:00:00', NULL, 7, NULL, '0');

SELECT '停车业务数据库初始化完成！' AS message;
SELECT COUNT(*) AS parking_space_count FROM parking_space;
SELECT COUNT(*) AS parking_record_count FROM owner_parking;
