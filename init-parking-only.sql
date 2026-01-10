-- ========================================
-- 停车业务数据库初始化脚本
-- Database: parking_business_db
-- ========================================

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

SELECT '========================================' AS '';
SELECT '停车业务数据库初始化完成！' AS message;
SELECT '停车位数量：' AS info, COUNT(*) AS count FROM parking_space;
SELECT '停车记录数量：' AS info, COUNT(*) AS count FROM owner_parking;
SELECT '========================================' AS '';
