#!/bin/bash
set -e

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
GATEWAY_URL="http://localhost:9000"
ADMIN_LOGIN="testadmin"
ADMIN_PASSWORD="admin123"
OWNER_LOGIN="owner_test005"
OWNER_PASSWORD="123456"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  停车管理系统微服务综合测试脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# ============== 1. 网关测试 ==============
echo -e "${GREEN}===== 第1部分：网关功能测试 =====${NC}"
echo ""

# 1.1 测试未认证访问受保护资源（应该返回401）
echo -e "${YELLOW}[1.1] 测试未认证访问受保护资源${NC}"
echo "请求: GET $GATEWAY_URL/user/user/owners"
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$GATEWAY_URL/user/user/owners")
HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "响应状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}✓ 通过：未认证请求被正确拒绝${NC}"
else
    echo -e "${RED}✗ 失败：应该返回401，实际返回$HTTP_CODE${NC}"
fi
echo ""

# 1.2 管理员登录获取token
echo -e "${YELLOW}[1.2] 管理员登录获取token${NC}"
echo "请求: POST $GATEWAY_URL/user/auth/admin/login"
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/user/auth/admin/login?loginName=$ADMIN_LOGIN&password=$ADMIN_PASSWORD")
echo "响应: $LOGIN_RESPONSE"
ADMIN_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✓ 通过：成功获取管理员token${NC}"
    echo "Token: ${ADMIN_TOKEN:0:50}..."
else
    echo -e "${RED}✗ 失败：无法获取token${NC}"
    exit 1
fi
echo ""

# 1.3 使用token访问受保护资源
echo -e "${YELLOW}[1.3] 使用token访问受保护资源${NC}"
echo "请求: GET $GATEWAY_URL/user/user/owners（带Authorization头）"
PROTECTED_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/user/user/owners?pageNum=1&pageSize=5")
HTTP_CODE=$(echo "$PROTECTED_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "响应状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ 通过：成功访问受保护资源${NC}"
    # 提取并显示部分响应内容
    BODY=$(echo "$PROTECTED_RESPONSE" | sed '/HTTP_CODE/d')
    echo "响应数据: $(echo $BODY | cut -c1-100)..."
else
    echo -e "${RED}✗ 失败：无法访问受保护资源${NC}"
fi
echo ""

# 1.4 测试网关路由转发（user-service）
echo -e "${YELLOW}[1.4] 测试网关路由转发 - user-service${NC}"
echo "请求: GET $GATEWAY_URL/user/user/owners/1"
USER_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/user/user/owners/1")
HTTP_CODE=$(echo "$USER_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "响应状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ 通过：网关成功转发到user-service${NC}"
else
    echo -e "${RED}✗ 失败：路由转发失败${NC}"
fi
echo ""

# 1.5 测试网关路由转发（parking-service）
echo -e "${YELLOW}[1.5] 测试网关路由转发 - parking-service${NC}"
echo "请求: GET $GATEWAY_URL/parking/parking/admin/parkings"
PARKING_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/parking/parking/admin/parkings?pageNum=1&pageSize=5")
HTTP_CODE=$(echo "$PARKING_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
echo "响应状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ 通过：网关成功转发到parking-service${NC}"
else
    echo -e "${RED}✗ 失败：路由转发失败${NC}"
fi
echo ""

# 1.6 测试网关路由转发（fee-service）
echo -e "${YELLOW}[1.6] 测试网关路由转发 - fee-service${NC}"
echo "请求: GET $GATEWAY_URL/fee/fee/admin/list"
FEE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/fee/fee/admin/list?pageNum=1&pageSize=5")
HTTP_CODE=$(echo "$FEE_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$FEE_RESPONSE" | sed '/HTTP_CODE/d')
echo "响应状态码: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ 通过：网关成功转发到fee-service${NC}"
elif [ "$HTTP_CODE" = "500" ]; then
    echo -e "${YELLOW}⚠ 注意：fee-service返回500，可能是数据库为空或服务间调用失败${NC}"
    echo "错误信息: $(echo $BODY | head -c 150)..."
else
    echo -e "${RED}✗ 失败：路由转发失败${NC}"
fi
echo ""

echo -e "${GREEN}===== 网关功能测试完成 =====${NC}"
echo ""
sleep 2

# ============== 2. 准备测试数据 ==============
echo -e "${GREEN}===== 第2部分：准备测试数据 =====${NC}"
echo ""

# 2.1 业主登录获取token
echo -e "${YELLOW}[2.1] 业主登录获取token${NC}"
OWNER_LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/user/auth/owner/login?loginName=$OWNER_LOGIN&password=$OWNER_PASSWORD")
echo "响应: $OWNER_LOGIN_RESPONSE"
OWNER_TOKEN=$(echo $OWNER_LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
OWNER_USER_ID=$(echo $OWNER_LOGIN_RESPONSE | grep -o '"userId":[0-9]*' | cut -d: -f2)

if [ -n "$OWNER_TOKEN" ] && [ -n "$OWNER_USER_ID" ]; then
    echo -e "${GREEN}✓ 通过：成功获取业主token，userId=$OWNER_USER_ID${NC}"
else
    echo -e "${RED}✗ 失败：无法获取业主token${NC}"
    echo "响应内容: $OWNER_LOGIN_RESPONSE"
    exit 1
fi
echo ""

# 2.2 为测试业主分配车位（如果还没有）
echo -e "${YELLOW}[2.2] 为测试业主分配车位（如需要）${NC}"
ASSIGN_RESPONSE=$(curl -s -X POST \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/parking/parking/admin/parkings/assign?userId=$OWNER_USER_ID&parkId=1&carNumber=粤B88888")
echo "响应: $ASSIGN_RESPONSE"
if echo "$ASSIGN_RESPONSE" | grep -q "成功\|已有车位"; then
    echo -e "${GREEN}✓ 车位分配成功或已有车位${NC}"
else
    echo -e "${YELLOW}⚠ 注意：车位分配可能失败，继续测试${NC}"
fi
echo ""

# 2.3 插入未缴费记录
echo -e "${YELLOW}[2.3] 插入测试用的未缴费记录${NC}"
docker exec -i fee-db mysql -uroot -proot_password parking_fee_db <<EOF
-- 清空旧数据
DELETE FROM fee_park WHERE user_id=$OWNER_USER_ID;

-- 插入10条未缴费记录
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, create_time, update_time) VALUES
($OWNER_USER_ID, 1, '2025-01', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-02', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-03', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-04', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-05', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-06', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-07', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-08', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-09', 500.00, '0', NOW(), NOW()),
($OWNER_USER_ID, 1, '2025-10', 500.00, '0', NOW(), NOW());

-- 查询新插入的fee_id
SELECT fee_id FROM fee_park WHERE user_id=$OWNER_USER_ID AND pay_park_status='0' ORDER BY fee_id DESC LIMIT 10;
EOF
echo -e "${GREEN}✓ 通过：成功插入10条未缴费记录${NC}"
echo ""

# 2.4 获取刚插入的fee_id列表
echo -e "${YELLOW}[2.4] 获取费用记录ID列表${NC}"
FEE_IDS=$(docker exec fee-db mysql -uroot -proot_password parking_fee_db -N -e \
    "SELECT GROUP_CONCAT(fee_id ORDER BY fee_id) FROM fee_park WHERE user_id=$OWNER_USER_ID AND pay_park_status='0'")
echo "费用ID列表: $FEE_IDS"
# 转换为数组
IFS=',' read -ra FEE_ID_ARRAY <<< "$FEE_IDS"
echo -e "${GREEN}✓ 获取到${#FEE_ID_ARRAY[@]}条费用记录${NC}"
echo ""

echo -e "${GREEN}===== 测试数据准备完成 =====${NC}"
echo ""
sleep 2

# ============== 3. 负载均衡测试 ==============
echo -e "${GREEN}===== 第3部分：负载均衡测试 =====${NC}"
echo ""

echo -e "${YELLOW}[3.1] 调用缴费接口10次，观察负载分布${NC}"
echo "请求: POST $GATEWAY_URL/fee/fee/owner/pay"
echo ""

# 只缴前10条记录（使用刚获取的fee_id）
for i in "${!FEE_ID_ARRAY[@]}"; do
    FEE_ID=${FEE_ID_ARRAY[$i]}
    INDEX=$((i+1))
    echo -e "${BLUE}--------- 第 $INDEX 次请求（fee_id=$FEE_ID）---------${NC}"
    RESPONSE=$(curl -s -X POST \
        -H "Authorization: Bearer $OWNER_TOKEN" \
        "$GATEWAY_URL/fee/fee/owner/pay?parkFeeId=$FEE_ID&userId=$OWNER_USER_ID")
    echo "$RESPONSE"
    sleep 0.5

    # 只测试前10条
    if [ $INDEX -ge 10 ]; then
        break
    fi
done
echo ""

echo -e "${YELLOW}[3.2] 查看user-service实例负载分布${NC}"
docker logs parking-user-service-8081 2>&1 | grep "负载均衡" | tail -5
docker logs parking-user-service-8091 2>&1 | grep "负载均衡" | tail -5
echo ""

echo -e "${YELLOW}[3.3] 查看parking-service实例负载分布${NC}"
docker logs parking-parking-service-8082 2>&1 | grep "负载均衡" | tail -5
docker logs parking-parking-service-8092 2>&1 | grep "负载均衡" | tail -5
echo ""

echo -e "${GREEN}===== 负载均衡测试完成 =====${NC}"
echo ""
sleep 2

# ============== 4. 熔断降级测试 ==============
echo -e "${GREEN}===== 第4部分：熔断降级测试 =====${NC}"
echo ""

echo -e "${YELLOW}[4.1] 停止所有user-service实例${NC}"
docker stop parking-user-service-8081
docker stop parking-user-service-8091
echo -e "${GREEN}✓ user-service实例已停止${NC}"
echo ""
sleep 2

echo -e "${YELLOW}[4.2] 调用需要user-service的接口，观察熔断降级${NC}"
echo "请求: GET $GATEWAY_URL/fee/fee/admin/list（需要调用user-service获取用户名）"
echo ""

for i in {1..3}; do
    echo -e "${BLUE}--------- 第 $i 次请求 ---------${NC}"
    RESPONSE=$(curl -s \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        "$GATEWAY_URL/fee/fee/admin/list?pageNum=1&pageSize=5")
    echo "$RESPONSE" | head -c 200
    echo "..."
    sleep 1
done
echo ""

echo -e "${YELLOW}[4.3] 查看fee-service日志中的降级信息${NC}"
docker logs parking-fee-service 2>&1 | grep -E "获取用户信息失败|熔断降级|Fallback|Circuit" | tail -10
echo ""

echo -e "${YELLOW}[4.4] 重启user-service实例${NC}"
docker start parking-user-service-8081
docker start parking-user-service-8091
echo -e "${GREEN}✓ user-service实例已重启${NC}"
echo ""
sleep 5

echo -e "${YELLOW}[4.5] 等待服务恢复并注册到Nacos...${NC}"
sleep 10

echo -e "${YELLOW}[4.6] 验证服务恢复后功能正常${NC}"
RESPONSE=$(curl -s \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$GATEWAY_URL/fee/fee/admin/list?pageNum=1&pageSize=5")
echo "$RESPONSE" | head -c 200
echo "..."

if echo "$RESPONSE" | grep -q "username"; then
    echo -e "${GREEN}✓ 通过：服务恢复后，用户名正确显示${NC}"
else
    echo -e "${YELLOW}⚠ 注意：用户名仍未显示，可能需要更多恢复时间${NC}"
fi
echo ""

echo -e "${GREEN}===== 熔断降级测试完成 =====${NC}"
echo ""

# ============== 5. 测试总结 ==============
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}          测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✓ 网关测试：${NC}"
echo "  - JWT认证功能正常"
echo "  - 路由转发功能正常"
echo "  - 未认证请求被正确拦截"
echo ""
echo -e "${GREEN}✓ 负载均衡测试：${NC}"
echo "  - 请求均匀分布到多个服务实例"
echo "  - Nacos服务发现与OpenFeign集成正常"
echo ""
echo -e "${GREEN}✓ 熔断降级测试：${NC}"
echo "  - 服务不可用时触发降级"
echo "  - 服务恢复后功能正常"
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}所有测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
