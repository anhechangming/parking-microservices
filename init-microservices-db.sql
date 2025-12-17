-- ========================================
-- 停车管理系统 - 微服务数据库初始化脚本
-- ========================================

-- ========================================
-- 1. 用户服务数据库 (parking_user_db)
-- ========================================
USE parking_user_db;

-- 系统管理员表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    login_name VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    username VARCHAR(50) NOT NULL COMMENT '用户昵称',
    phone VARCHAR(20) COMMENT '手机号码',
    sex CHAR(1) DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
    status CHAR(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_login_name (login_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- 业主表
CREATE TABLE IF NOT EXISTS live_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业主ID',
    login_name VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    username VARCHAR(50) NOT NULL COMMENT '业主姓名',
    phone VARCHAR(20) COMMENT '手机号码',
    sex CHAR(1) DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
    id_card VARCHAR(18) COMMENT '身份证号',
    user_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '用户类型（NORMAL普通 VIP会员）',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_login_name (login_name),
    INDEX idx_status (status),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业主表';

-- 初始化管理员账号（密码：admin123）
INSERT INTO sys_user (login_name, password, username, phone, sex, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '系统管理员', '13800138000', '0', '0')
ON DUPLICATE KEY UPDATE username=username;

-- 初始化测试业主账号（密码：owner123）
INSERT INTO live_user (login_name, password, username, phone, sex, id_card, user_type, status)
VALUES
    ('owner001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '张三', '13900139001', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '李四', '13900139002', '0', '110101199001011235', 'VIP', '0'),
    ('owner003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '王五', '13900139003', '0', '110101199001011236', 'NORMAL', '0')
ON DUPLICATE KEY UPDATE username=username;

-- ========================================
-- 2. 停车业务服务数据库 (parking_business_db)
-- ========================================
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

-- 初始化停车位数据
INSERT INTO parking_space (park_num, park_type, park_status, remark)
VALUES
    ('A-001', '0', '0', '地面停车位'),
    ('A-002', '0', '0', '地面停车位'),
    ('A-003', '0', '0', '地面停车位'),
    ('B-001', '1', '0', '地下停车位'),
    ('B-002', '1', '0', '地下停车位'),
    ('B-003', '1', '0', '地下停车位')
ON DUPLICATE KEY UPDATE park_num=park_num;

-- 初始化测试停车记录（为跨服务调用测试准备数据）
INSERT INTO owner_parking (user_id, park_id, car_num, entry_time, exit_time, parking_days, parking_fee, payment_status)
VALUES
    (1, 1, '京A12345', '2025-12-01 08:00:00', '2025-12-10 18:00:00', 9, 900.00, '0'),
    (2, 2, '京B67890', '2025-12-05 09:00:00', '2025-12-15 17:00:00', 10, 1000.00, '0'),
    (3, 3, '京C11111', '2025-12-10 10:00:00', NULL, 7, NULL, '0')
ON DUPLICATE KEY UPDATE id=id;

-- ========================================
-- 3. 费用服务数据库 (parking_fee_db)
-- ========================================
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

-- 初始化测试费用记录（为跨服务调用测试准备数据）
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, pay_time)
VALUES
    (1, 1, '2025-12', 900.00, '0', NULL),
    (2, 2, '2025-12', 1000.00, '0', NULL),
    (1, 1, '2025-11', 800.00, '1', '2025-11-30 10:00:00'),
    (2, 2, '2025-11', 750.00, '1', '2025-11-29 15:00:00')
ON DUPLICATE KEY UPDATE fee_id=fee_id;

-- ========================================
-- 初始化完成！
-- ========================================
SELECT '用户服务数据库初始化完成' AS message;
SELECT '停车服务数据库初始化完成' AS message;
SELECT '费用服务数据库初始化完成' AS message;
