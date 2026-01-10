-- ========================================
-- 停车管理系统 - 完整数据库初始化脚本
-- ========================================

-- ========================================
-- 1. 用户服务数据库 (parking_user_db)
-- ========================================
USE parking_user_db;

-- 清空现有数据
TRUNCATE TABLE sys_user;
TRUNCATE TABLE live_user;

-- 初始化管理员账号
-- testadmin 密码：admin123 - 请替换为正确的BCrypt哈希值
-- admin 密码：admin123
INSERT INTO sys_user (login_name, password, username, phone, sex, status)
VALUES
    ('admin', '$2a$10$0C73VeCaRqtQCLPrge2Ml.KHiFb4OIdXg.qhaUxzaHCsuDaCsS3iG', '系统管理员', '13800138000', '0', '0'),
    ('testadmin', '$2a$10$0C73VeCaRqtQCLPrge2Ml.KHiFb4OIdXg.qhaUxzaHCsuDaCsS3iG', '测试管理员', '13800138001', '0', '0');

-- 初始化业主账号
-- owner001, owner002, owner003 密码：123456
-- owner_test005 密码：123456 - 请替换为正确的BCrypt哈希值
-- owner1, owner6, owner7 使用现有密码哈希
INSERT INTO live_user (login_name, password, username, phone, sex, id_card, user_type, status)
VALUES
    ('owner001', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', 'tom', '13900139001', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner002', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', 'Bob', '13900139002', '0', '110101199001011235', 'VIP', '0'),
    ('owner003', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主3', '13900139003', '0', '110101199001011236', 'NORMAL', '0'),
    ('owner1', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主1', '13800138000', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner_test005', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '测试业主', '13900139005', '1', '110101199001011238', 'VIP', '0'),
    ('owner6', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主6', '16651359008', '0', '110101199506182347', 'NORMAL', '0'),
    ('owner7', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主7', '13700137007', '0', '110101199601011237', 'NORMAL', '0');

SELECT '用户数据库初始化完成！' AS message;
SELECT '管理员账号数量：' AS info, COUNT(*) AS count FROM sys_user;
SELECT '业主账号数量：' AS info, COUNT(*) AS count FROM live_user;

-- ========================================
-- 2. 停车业务数据库 (parking_business_db)
-- ========================================
USE parking_business_db;

-- 清空现有数据
TRUNCATE TABLE owner_parking;
TRUNCATE TABLE parking_space;

-- 初始化停车位数据
INSERT INTO parking_space (park_id, park_num, park_type, park_status, remark)
VALUES
    (1, 'A-001', 2, 1, '地面停车位'),
    (2, 'A-002', 0, 1, '地面停车位'),
    (3, 'A-003', 0, 1, '地面停车位'),
    (4, 'B-001', 1, 0, '地下停车位'),
    (5, 'B-002', 1, 0, '地下停车位'),
    (6, 'B-003', 1, 1, '地下停车位'),
    (7, 'B-004', 0, 1, '地下停车位'),
    (8, 'B-005', 1, 0, '地下停车位'),
    (9, 'C-001', 0, 1, '立体车库'),
    (10, 'C-002', 0, 1, '立体车库');

-- 初始化停车记录数据
INSERT INTO owner_parking (user_id, park_id, car_num, entry_time, exit_time, parking_days, parking_fee, payment_status)
VALUES
    (1, 1, '京A12345', '2025-12-01 08:00:00', '2025-12-10 18:00:00', 9, 900.00, 0),
    (2, 2, '京B67890', '2025-12-05 09:00:00', '2025-12-15 17:00:00', 10, 1000.00, 0),
    (3, 3, '京C11111', '2025-12-10 10:00:00', NULL, 7, NULL, 0),
    (5, 1, '京B12345', '2025-12-27 17:11:37', '2025-12-28 17:21:17', 0, NULL, 0),
    (1, 6, '京B33333', '2025-12-27 19:51:43', NULL, 0, NULL, 1),
    (2, 2, '京BTEST2', '2025-12-27 20:14:56', NULL, 0, NULL, 1);

SELECT '停车业务数据库初始化完成！' AS message;
SELECT '停车位数量：' AS info, COUNT(*) AS count FROM parking_space;
SELECT '停车记录数量：' AS info, COUNT(*) AS count FROM owner_parking;

-- ========================================
-- 3. 费用服务数据库 (parking_fee_db)
-- ========================================
USE parking_fee_db;

-- 清空现有数据
TRUNCATE TABLE fee_park;

-- 初始化费用记录数据
-- 为user_id=1的业主创建10个月费用记录（未缴费）
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, pay_time)
VALUES
    (1, 1, '2025-01', 500.00, 0, NULL),
    (1, 1, '2025-02', 500.00, 0, NULL),
    (1, 1, '2025-03', 500.00, 0, NULL),
    (1, 1, '2025-04', 500.00, 0, NULL),
    (1, 1, '2025-05', 500.00, 0, NULL),
    (1, 1, '2025-06', 500.00, 0, NULL),
    (1, 1, '2025-07', 500.00, 0, NULL),
    (1, 1, '2025-08', 500.00, 0, NULL),
    (1, 1, '2025-09', 500.00, 0, NULL),
    (1, 1, '2025-10', 500.00, 0, NULL);

-- 为user_id=5的业主创建9个月费用记录（已缴费）
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, pay_time)
VALUES
    (5, 1, '2025-01', 500.00, 1, '2025-12-28 19:33:55'),
    (5, 1, '2025-02', 500.00, 1, '2025-12-28 19:33:55'),
    (5, 1, '2025-03', 500.00, 1, '2025-12-28 19:33:56'),
    (5, 1, '2025-04', 500.00, 1, '2025-12-28 19:33:56'),
    (5, 1, '2025-05', 500.00, 1, '2025-12-28 19:33:57'),
    (5, 1, '2025-06', 500.00, 1, '2025-12-28 19:33:58'),
    (5, 1, '2025-07', 500.00, 1, '2025-12-28 19:33:58'),
    (5, 1, '2025-08', 500.00, 1, '2025-12-28 19:33:59'),
    (5, 1, '2025-09', 500.00, 1, '2025-12-28 19:34:00');

-- 为其他业主创建费用记录
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, pay_time)
VALUES
    (2, 2, '2025-01', 500.00, 0, NULL),
    (2, 2, '2025-02', 500.00, 0, NULL),
    (3, 3, '2025-01', 500.00, 0, NULL),
    (3, 3, '2025-02', 500.00, 0, NULL),
    (4, 4, '2025-01', 500.00, 0, NULL),
    (6, 6, '2025-01', 500.00, 1, '2025-12-28 10:00:00');

SELECT '费用数据库初始化完成！' AS message;
SELECT '费用记录数量：' AS info, COUNT(*) AS count FROM fee_park;
SELECT '已缴费记录：' AS info, COUNT(*) AS count FROM fee_park WHERE pay_park_status = '1';
SELECT '未缴费记录：' AS info, COUNT(*) AS count FROM fee_park WHERE pay_park_status = '0';

-- ========================================
-- 初始化完成总结
-- ========================================
SELECT '========================================' AS '';
SELECT '所有数据库初始化完成！' AS message;
SELECT '========================================' AS '';

