-- 用户服务数据库初始化
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

-- 清空现有数据
TRUNCATE TABLE sys_user;
TRUNCATE TABLE live_user;

-- 初始化管理员账号（密码：admin123）
INSERT INTO sys_user (login_name, password, username, phone, sex, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '系统管理员', '13800138000', '0', '0');

-- 初始化测试业主账号（密码：owner123）
INSERT INTO live_user (login_name, password, username, phone, sex, id_card, user_type, status)
VALUES
    ('owner001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '张三', '13900139001', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '李四', '13900139002', '0', '110101199001011235', 'VIP', '0'),
    ('owner003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Ehvp', '王五', '13900139003', '0', '110101199001011236', 'NORMAL', '0');

SELECT '用户数据库初始化完成！' AS message;
SELECT COUNT(*) AS admin_count FROM sys_user;
SELECT COUNT(*) AS owner_count FROM live_user;
