-- ========================================
-- 用户服务数据库初始化脚本
-- Database: parking_user_db
-- ========================================

-- 清空现有数据
TRUNCATE TABLE sys_user;
TRUNCATE TABLE live_user;

-- 初始化管理员账号
-- admin 密码：admin123
-- testadmin 密码：admin123
INSERT INTO sys_user (login_name, password, username, phone, sex, status)
VALUES
    ('admin', '$2a$10$0C73VeCaRqtQCLPrge2Ml.KHiFb4OIdXg.qhaUxzaHCsuDaCsS3iG', '系统管理员', '13800138000', '0', '0'),
    ('testadmin', '$2a$10$0C73VeCaRqtQCLPrge2Ml.KHiFb4OIdXg.qhaUxzaHCsuDaCsS3iG', '测试管理员', '13800138001', '0', '0');

-- 初始化业主账号
-- 所有业主密码：123456
INSERT INTO live_user (login_name, password, username, phone, sex, id_card, user_type, status)
VALUES
    ('owner001', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', 'tom', '13900139001', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner002', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', 'Bob', '13900139002', '0', '110101199001011235', 'VIP', '0'),
    ('owner003', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主3', '13900139003', '0', '110101199001011236', 'NORMAL', '0'),
    ('owner1', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主1', '13800138000', '0', '110101199001011234', 'NORMAL', '0'),
    ('owner_test005', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '测试业主', '13900139005', '1', '110101199001011238', 'VIP', '0'),
    ('owner6', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主6', '16651359008', '0', '110101199506182347', 'NORMAL', '0'),
    ('owner7', '$2a$10$t9DrvCqNOI.B0eaVlZX1V.nTuhn.gO8V/7BMOVQn2VKLltJ0ezBRe', '业主7', '13700137007', '0', '110101199601011237', 'NORMAL', '0');

SELECT '========================================' AS '';
SELECT '用户数据库初始化完成！' AS message;
SELECT '管理员账号数量：' AS info, COUNT(*) AS count FROM sys_user;
SELECT '业主账号数量：' AS info, COUNT(*) AS count FROM live_user;
SELECT '========================================' AS '';
