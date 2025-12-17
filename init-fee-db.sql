-- 费用服务数据库初始化
USE parking_fee_db;

-- 停车费用记录表
CREATE TABLE IF NOT EXISTS fee_park (
    fee_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '费用ID',
    user_id BIGINT NOT NULL COMMENT '业主ID',
    park_id BIGINT NOT NULL COMMENT '停车位ID',
    pay_park_month VARCHAR(7) NOT NULL COMMENT '缴费月份（格式：2025-12）',
    pay_park_money DECIMAL(10,2) NOT NULL COMMENT '缴费金额',
    pay_park_status CHAR(1) DEFAULT '0' COMMENT '缴费状态（0未缴费 1已缴费）',
    pay_time DATETIME COMMENT '缴费时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_park_id (park_id),
    INDEX idx_pay_park_month (pay_park_month),
    INDEX idx_pay_park_status (pay_park_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车费用记录表';

-- 清空现有数据
TRUNCATE TABLE fee_park;

-- 初始化测试费用记录（为跨服务调用测试准备数据）
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, pay_time)
VALUES
    (1, 1, '2025-12', 900.00, '0', NULL),
    (2, 2, '2025-12', 1000.00, '0', NULL),
    (1, 1, '2025-11', 800.00, '1', '2025-11-30 10:00:00'),
    (2, 2, '2025-11', 750.00, '1', '2025-11-29 15:00:00');

SELECT '费用数据库初始化完成！' AS message;
SELECT COUNT(*) AS fee_record_count FROM fee_park;
