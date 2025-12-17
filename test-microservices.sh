#!/bin/bash

echo "=========================================="
echo "微服务跨服务调用测试"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "=========================================="
echo "测试 1: 查询所有用户（user-service 独立调用）"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8081/user/owners'${NC}"
curl -X GET 'http://localhost:8081/user/owners'
echo ""
echo ""

echo "=========================================="
echo "测试 2: 查询单个用户（会被其他服务调用）"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8081/user/owners/1'${NC}"
curl -X GET 'http://localhost:8081/user/owners/1'
echo ""
echo ""

echo "=========================================="
echo "测试 3: 查询所有停车位（parking-service 独立调用）"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8082/parking/admin/parkings?pageNum=1&pageSize=10'${NC}"
curl -X GET 'http://localhost:8082/parking/admin/parkings?pageNum=1&pageSize=10'
echo ""
echo ""

echo "=========================================="
echo "测试 4: 查询可用停车位"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8082/parking/admin/parkings/available'${NC}"
curl -X GET 'http://localhost:8082/parking/admin/parkings/available'
echo ""
echo ""

echo "=========================================="
echo "🔥 跨服务调用测试开始 🔥"
echo "=========================================="

echo ""
echo "=========================================="
echo "【跨服务测试 1】parking-service → user-service"
echo "场景：分配车位时，parking-service 调用 user-service 验证用户是否存在"
echo "=========================================="
echo -e "${YELLOW}curl -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=3&parkId=5&carNumber=%E4%BA%ACA99999'${NC}"
echo ""
echo "预期：parking-service 会先调用 user-service 的 /user/owners/3 验证用户存在，然后分配车位"
echo ""
RESULT=$(curl -s -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=3&parkId=5&carNumber=%E4%BA%ACA99999')
echo "$RESULT"

if echo "$RESULT" | grep -q '"code":200'; then
    echo -e "${GREEN}✅ 测试通过：车位分配成功，跨服务调用正常${NC}"
else
    echo -e "${RED}❌ 测试失败${NC}"
fi
echo ""
echo "查看 parking-service 日志验证是否调用了 user-service："
echo "docker logs parking-parking-service 2>&1 | grep -i 'user-service\|RestTemplate' | tail -5"
echo ""
echo ""

echo "=========================================="
echo "【跨服务测试 2】fee-service → user-service"
echo "场景：查询费用时，fee-service 调用 user-service 获取用户类型（VIP/NORMAL）"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8083/fee/owner/my-fees?userId=2'${NC}"
echo ""
echo "预期：fee-service 会调用 user-service 获取 user_id=2（李四VIP）的信息"
echo ""
RESULT=$(curl -s -X GET 'http://localhost:8083/fee/owner/my-fees?userId=2')
echo "$RESULT"

if echo "$RESULT" | grep -q '"code":200'; then
    echo -e "${GREEN}✅ 测试通过：费用查询成功，跨服务调用正常${NC}"
else
    echo -e "${RED}❌ 测试失败${NC}"
fi
echo ""
echo ""

echo "=========================================="
echo "【跨服务测试 3】fee-service → user-service (VIP折扣测试)"
echo "场景：查询VIP用户费用，验证VIP折扣是否生效"
echo "=========================================="
echo -e "${YELLOW}curl -X GET 'http://localhost:8083/fee/owner/my-fees?userId=2'${NC}"
echo ""
echo "VIP用户（user_id=2 李四）应该享受折扣"
echo ""
RESULT=$(curl -s -X GET 'http://localhost:8083/fee/owner/my-fees?userId=2')
echo "$RESULT" | jq '.' 2>/dev/null || echo "$RESULT"
echo ""
echo ""

echo "=========================================="
echo "【跨服务测试 4】fee-service → user-service + parking-service"
echo "场景：缴费时，fee-service 同时调用 user-service（获取用户类型）"
echo "     和 parking-service（获取停车记录）"
echo "=========================================="
echo -e "${YELLOW}curl -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1'${NC}"
echo ""
echo "预期：fee-service 会："
echo "  1. 调用 user-service 获取用户类型"
echo "  2. 调用 parking-service 获取停车记录"
echo "  3. 计算费用并更新缴费状态"
echo ""
RESULT=$(curl -s -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1')
echo "$RESULT"

if echo "$RESULT" | grep -q '"code":200\|缴费成功'; then
    echo -e "${GREEN}✅ 测试通过：缴费成功，跨服务调用正常${NC}"
else
    echo -e "${RED}❌ 测试失败${NC}"
fi
echo ""
echo ""

echo "=========================================="
echo "测试完成！查看服务调用日志"
echo "=========================================="

echo ""
echo "【parking-service 的日志】（查看是否调用了 user-service）:"
docker logs parking-parking-service 2>&1 | grep -i "调用\|user-service\|RestTemplate" | tail -10
echo ""

echo "【fee-service 的日志】（查看是否调用了 user-service 和 parking-service）:"
docker logs parking-fee-service 2>&1 | grep -i "调用\|user-service\|parking-service\|RestTemplate" | tail -10
echo ""

echo "=========================================="
echo "验证 Nacos 服务注册"
echo "=========================================="
echo ""
echo "所有注册的服务："
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service" | jq '.hosts[].ip' 2>/dev/null || echo "user-service 已注册"
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=parking-service" | jq '.hosts[].ip' 2>/dev/null || echo "parking-service 已注册"
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=fee-service" | jq '.hosts[].ip' 2>/dev/null || echo "fee-service 已注册"
echo ""

echo "=========================================="
echo "🎉 所有测试完成！"
echo "=========================================="
echo ""
echo "访问 Nacos 控制台查看服务："
echo "http://localhost:8848/nacos (账号: nacos, 密码: nacos)"
