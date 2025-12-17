#!/bin/bash

echo "=========================================="
echo "开始初始化微服务数据库..."
echo "=========================================="

# 初始化用户数据库
echo ""
echo "1. 初始化 user-db (parking_user_db)..."
docker exec -i user-db mysql -uroot -proot_password < init-user-db.sql

if [ $? -eq 0 ]; then
    echo "✅ user-db 初始化成功"
else
    echo "❌ user-db 初始化失败"
    exit 1
fi

# 初始化停车业务数据库
echo ""
echo "2. 初始化 parking-db (parking_business_db)..."
docker exec -i parking-db mysql -uroot -proot_password < init-parking-db.sql

if [ $? -eq 0 ]; then
    echo "✅ parking-db 初始化成功"
else
    echo "❌ parking-db 初始化失败"
    exit 1
fi

# 初始化费用数据库
echo ""
echo "3. 初始化 fee-db (parking_fee_db)..."
docker exec -i fee-db mysql -uroot -proot_password < init-fee-db.sql

if [ $? -eq 0 ]; then
    echo "✅ fee-db 初始化成功"
else
    echo "❌ fee-db 初始化失败"
    exit 1
fi

echo ""
echo "=========================================="
echo "🎉 所有数据库初始化完成！"
echo "=========================================="

# 验证数据
echo ""
echo "=========================================="
echo "验证数据插入情况..."
echo "=========================================="

echo ""
echo "【用户数据】"
docker exec -i user-db mysql -uroot -proot_password -e "
USE parking_user_db;
SELECT user_id, username, user_type, phone FROM live_user;
"

echo ""
echo "【停车位数据】"
docker exec -i parking-db mysql -uroot -proot_password -e "
USE parking_business_db;
SELECT park_id, park_num, park_type, park_status FROM parking_space;
"

echo ""
echo "【停车记录】"
docker exec -i parking-db mysql -uroot -proot_password -e "
USE parking_business_db;
SELECT id, user_id, car_num, parking_days, parking_fee, payment_status FROM owner_parking;
"

echo ""
echo "【费用记录】"
docker exec -i fee-db mysql -uroot -proot_password -e "
USE parking_fee_db;
SELECT fee_id, user_id, pay_park_month, pay_park_money, pay_park_status FROM fee_park;
"

echo ""
echo "=========================================="
echo "✅ 数据初始化和验证完成！"
echo "=========================================="
echo ""
echo "现在可以开始测试跨服务调用了！"
echo "请参考: 跨服务调用测试指南.md"
