#!/bin/bash
# 异步消息队列测试脚本（带控制台可视化输出）
# 功能：自动获取Token、发布消息、验证消费、查看队列状态

# 定义颜色（控制台高亮输出，更易区分）
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # 恢复默认颜色

# ===================== 步骤1：自动获取登录Token =====================
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}步骤1：获取登录Token（鉴权用）${NC}"
echo -e "${BLUE}====================================${NC}"

# 发送登录请求，提取Token
TOKEN_RESPONSE=$(curl -s -X POST "http://localhost:9000/user/auth/admin/login?loginName=testadmin&password=admin123")
# 提取Token（适配返回格式：{"token":"xxx"} 或 {"data":{"token":"xxx"}}）
TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"token":"[^"]*' | cut -d '"' -f 4)

# 验证Token是否获取成功
if [ -z "$TOKEN" ]; then
    echo -e "${RED}[失败] 未获取到Token，登录失败！返回信息：$TOKEN_RESPONSE${NC}"
    exit 1
else
    echo -e "${GREEN}[成功] 获取到Token：$TOKEN${NC}"
    echo -e "${YELLOW}------------------------------------${NC}"
fi

# ===================== 步骤2：准备测试环境（先退还车位确保可用） =====================
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}步骤2：准备测试环境（退还车位）${NC}"
echo -e "${BLUE}====================================${NC}"

# 先退还这些用户的车位，确保车位可用
test_user_ids=(5 3 4)
for userId in "${test_user_ids[@]}"; do
    echo -e "${YELLOW}[正在执行] 退还用户 $userId 的车位${NC}"
    RETURN_RESPONSE=$(curl -s -X POST "http://localhost:9000/parking/parking/admin/parkings/return?userId=$userId" \
        -H "Authorization: Bearer $TOKEN")
    # 如果返回成功或"没有分配车位"都视为正常（确保车位可用）
    if echo "$RETURN_RESPONSE" | grep -qE "成功|没有分配车位"; then
        echo "返回：$RETURN_RESPONSE [OK]"
    else
        echo "返回：$RETURN_RESPONSE"
    fi
    sleep 0.5
done
echo -e "${GREEN}[完成] 车位准备完成${NC}"
echo -e "${YELLOW}------------------------------------${NC}"
sleep 1

# ===================== 步骤3：发布消息（车位分配，触发MQ消息） =====================
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}步骤3：发布MQ消息（执行车位分配）${NC}"
echo -e "${BLUE}====================================${NC}"

# 定义3个车位分配请求（批量发布消息）
assign_requests=(
    "userId=5&parkId=1&carNumber=B-12345"
    "userId=3&parkId=3&carNumber=B-88888"
    "userId=4&parkId=4&carNumber=B-99999"
)

# 循环执行车位分配，发布消息
for req in "${assign_requests[@]}"; do
    echo -e "${YELLOW}[正在执行] 车位分配：$req${NC}"
    # 发送请求，携带Token
    ASSIGN_RESPONSE=$(curl -s -X POST "http://localhost:9000/parking/parking/admin/parkings/assign?$req" \
        -H "Authorization: Bearer $TOKEN")
    # 打印执行结果
    if echo "$ASSIGN_RESPONSE" | grep -q "成功"; then
        echo -e "${GREEN}[发布成功] 车位分配完成，已触发MQ消息发布！返回：$ASSIGN_RESPONSE${NC}"
    else
        echo -e "${RED}[发布失败] 车位分配失败，未触发MQ消息！返回：$ASSIGN_RESPONSE${NC}"
    fi
    echo -e "${YELLOW}------------------------------------${NC}"
    sleep 1 # 间隔1秒，避免请求过快
done

# 执行车位退还，额外发布1条消息
echo -e "${YELLOW}[正在执行] 车位退还：userId=4${NC}"
RETURN_RESPONSE=$(curl -s -X POST "http://localhost:9000/parking/parking/admin/parkings/return?userId=4" \
    -H "Authorization: Bearer $TOKEN")
if echo "$RETURN_RESPONSE" | grep -q "成功"; then
    echo -e "${GREEN}[发布成功] 车位退还完成，已触发MQ消息发布！返回：$RETURN_RESPONSE${NC}"
else
    echo -e "${RED}[发布失败] 车位退还失败，未触发MQ消息！返回：$RETURN_RESPONSE${NC}"
fi
echo -e "${YELLOW}------------------------------------${NC}"
sleep 2 # 等待2秒，让MQ有时间传递消息

# ===================== 步骤4：控制台查看消息消费状态（核心可视化） =====================
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}步骤4：控制台查看MQ消息消费状态${NC}"
echo -e "${BLUE}====================================${NC}"

# 1. 查看 parking-service 消息发布日志（控制台打印）
echo -e "${YELLOW}[消息发布日志] parking-service 发布的MQ消息：${NC}"
docker logs parking-parking-service-8082 --tail 20 2>/dev/null | grep -iE "发布|rabbitmq|消息" || echo "实例8082: 无相关日志"
docker logs parking-parking-service-8092 --tail 20 2>/dev/null | grep -iE "发布|rabbitmq|消息" || echo "实例8092: 无相关日志"
echo -e "${YELLOW}------------------------------------${NC}"

# 2. 查看 fee-service 消息消费日志（控制台打印，关键：是否成功消费）
echo -e "${YELLOW}[消息消费日志] fee-service 消费的MQ消息（核心验证）：${NC}"
CONSUME_LOG=$(docker logs parking-fee-service --tail 30 2>/dev/null | grep -iE "成功创建费用记录|消费|rabbitmq|消息" || true)
if [ -z "$CONSUME_LOG" ]; then
    echo -e "${RED}[消费状态] 未找到消息消费记录，可能消息未被消费！${NC}"
else
    echo -e "${GREEN}[消费状态] 消息消费成功！消费日志如下：${NC}"
    echo "$CONSUME_LOG"
fi
echo -e "${YELLOW}------------------------------------${NC}"

# ===================== 步骤5：控制台查看RabbitMQ队列状态 =====================
echo -e "${BLUE}====================================${NC}"
echo -e "${BLUE}步骤5：控制台查看RabbitMQ队列状态${NC}"
echo -e "${BLUE}====================================${NC}"

echo -e "${YELLOW}[RabbitMQ队列信息] 当前队列列表（消息总数/队列名）：${NC}"
# 执行RabbitMQ队列查询，控制台输出
docker exec parking-rabbitmq rabbitmqctl list_queues 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}[成功] RabbitMQ队列状态查询完成！${NC}"
else
    echo -e "${RED}[失败] RabbitMQ容器未启动，无法查询队列状态！${NC}"
fi
echo -e "${YELLOW}------------------------------------${NC}"

# ===================== 步骤6：脚本执行完成 =====================
echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}脚本执行完成！核心结论：${NC}"
echo -e "${GREEN}1. 消息发布：查看步骤3的「发布成功」提示，确认消息已发送到MQ${NC}"
echo -e "${GREEN}2. 消息消费：查看步骤4的「消费成功」日志，确认消息已被处理${NC}"
echo -e "${GREEN}3. 队列状态：查看步骤5的RabbitMQ队列，确认无未消费积压（可选）${NC}"
echo -e "${GREEN}====================================${NC}"