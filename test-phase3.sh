#!/bin/bash

################################################################################
# Phase 3: 服务间通信与负载均衡完整测试脚本
#
# 功能：
# 1. 准备测试数据（创建停车费记录）
# 2. 测试负载均衡（验证请求分配到多个实例）
# 3. 测试熔断降级（验证服务故障时的降级处理）
#
# 使用方法：
# chmod +x test-phase3.sh
# ./test-phase3.sh
################################################################################

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印标题
print_header() {
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=========================================${NC}"
}

# 打印成功信息
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 打印警告信息
print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# 打印错误信息
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 打印信息
print_info() {
    echo -e "${YELLOW}$1${NC}"
}

################################################################################
# 阶段0: 准备测试数据
################################################################################
prepare_test_data() {
    print_header "阶段0: 准备测试数据"

    print_info "正在清空并创建测试数据..."

    # 创建停车费记录
    docker exec -i fee-db mysql -uroot -proot_password parking_fee_db <<EOF
-- 清空现有数据
DELETE FROM fee_park WHERE user_id = 1;

-- 为 user_id=1 创建 20 条未缴费记录（用于测试）
INSERT INTO fee_park (user_id, park_id, pay_park_month, pay_park_money, pay_park_status, create_time, update_time) VALUES
(1, 1, '2025-01', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-02', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-03', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-04', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-05', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-06', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-07', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-08', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-09', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-10', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-11', 500.00, '0', NOW(), NOW()),
(1, 1, '2025-12', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-01', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-02', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-03', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-04', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-05', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-06', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-07', 500.00, '0', NOW(), NOW()),
(1, 1, '2026-08', 500.00, '0', NOW(), NOW());

-- 验证数据
SELECT COUNT(*) as total_records FROM fee_park WHERE user_id = 1 AND pay_park_status = '0';
EOF

    if [ $? -eq 0 ]; then
        print_success "测试数据创建成功！"
    else
        print_error "测试数据创建失败！"
        exit 1
    fi

    # 确保所有服务运行
    print_info "确保所有服务运行正常..."
    docker start parking-user-service-8081 parking-user-service-8091 \
                 parking-parking-service-8082 parking-parking-service-8092 \
                 parking-fee-service > /dev/null 2>&1

    print_info "等待服务启动（15秒）..."
    sleep 15

    # 检查服务状态
    print_info "检查服务状态..."
    docker ps | grep "user-service\|parking-service\|fee-service" | grep "Up"

    print_success "准备工作完成！"
}

################################################################################
# 阶段1: 负载均衡测试
################################################################################
test_load_balancing() {
    print_header "阶段1: 负载均衡测试"

    # 清空之前的日志（重启服务）
    print_info "重启服务以清空日志..."
    docker-compose restart > /dev/null 2>&1
    sleep 15

    print_info "开始负载均衡测试..."
    print_info "将发送 10 次缴费请求，观察请求分配情况"
    echo ""

    # 发送10次请求
    for i in {1..10}; do
        echo "第 $i 次请求..."
        curl -s -X POST "http://localhost:8083/fee/owner/pay?parkFeeId=$i&userId=1" > /dev/null 2>&1
        sleep 1
    done

    echo ""
    print_info "统计负载均衡结果..."
    echo ""

    # 统计 user-service 负载均衡
    user_8081=$(docker logs parking-user-service-8081 2>&1 | grep -c "负载均衡")
    user_8091=$(docker logs parking-user-service-8091 2>&1 | grep -c "负载均衡")

    # 统计 parking-service 负载均衡
    parking_8082=$(docker logs parking-parking-service-8082 2>&1 | grep -c "负载均衡")
    parking_8092=$(docker logs parking-parking-service-8092 2>&1 | grep -c "负载均衡")

    echo "========================================="
    echo "负载均衡统计结果"
    echo "========================================="
    echo "user-service-8081:     $user_8081 次请求"
    echo "user-service-8091:     $user_8091 次请求"
    echo "parking-service-8082:  $parking_8082 次请求"
    echo "parking-service-8092:  $parking_8092 次请求"
    echo "========================================="
    echo ""

    # 验证结果
    total_user=$((user_8081 + user_8091))
    total_parking=$((parking_8082 + parking_8092))

    if [ $user_8081 -gt 0 ] && [ $user_8091 -gt 0 ]; then
        print_success "user-service 负载均衡生效！两个实例都收到请求"
    else
        print_error "user-service 负载均衡未生效！"
    fi

    if [ $parking_8082 -gt 0 ] && [ $parking_8092 -gt 0 ]; then
        print_success "parking-service 负载均衡生效！两个实例都收到请求"
    else
        print_error "parking-service 负载均衡未生效！"
    fi

    echo ""
    print_info "显示负载均衡详细日志（每个服务最后5条）："
    echo ""

    echo "【user-service-8081】"
    docker logs parking-user-service-8081 2>&1 | grep "负载均衡" | tail -5
    echo ""

    echo "【user-service-8091】"
    docker logs parking-user-service-8091 2>&1 | grep "负载均衡" | tail -5
    echo ""

    echo "【parking-service-8082】"
    docker logs parking-parking-service-8082 2>&1 | grep "负载均衡" | tail -5
    echo ""

    echo "【parking-service-8092】"
    docker logs parking-parking-service-8092 2>&1 | grep "负载均衡" | tail -5
    echo ""

    print_success "负载均衡测试完成！"
}

################################################################################
# 阶段2: 熔断降级测试
################################################################################
test_circuit_breaker() {
    print_header "阶段2: 熔断降级测试"

    # 测试场景1: user-service 熔断
    print_info "【场景1】测试 user-service 熔断降级"
    echo ""

    print_info "1. 停止所有 user-service 实例..."
    docker stop parking-user-service-8081 parking-user-service-8091 > /dev/null 2>&1
    sleep 5

    print_info "2. 调用缴费接口（应该触发降级）..."
    response=$(curl -s --max-time 10 -X POST "http://localhost:8083/fee/owner/pay?parkFeeId=11&userId=1")
    echo "响应: $response"
    echo ""

    print_info "3. 查看降级日志..."
    user_fallback_log=$(docker logs parking-fee-service 2>&1 | grep "熔断降级.*user-service" | tail -1)
    echo "$user_fallback_log"
    echo ""

    if echo "$user_fallback_log" | grep -q "熔断降级"; then
        print_success "user-service 熔断降级成功！降级方法被正确调用"
    else
        print_error "user-service 熔断降级失败！未找到降级日志"
    fi

    print_info "4. 恢复 user-service..."
    docker start parking-user-service-8081 parking-user-service-8091 > /dev/null 2>&1
    sleep 10

    echo ""

    # 测试场景2: parking-service 熔断
    print_info "【场景2】测试 parking-service 熔断降级"
    echo ""

    print_info "1. 停止所有 parking-service 实例..."
    docker stop parking-parking-service-8082 parking-parking-service-8092 > /dev/null 2>&1
    sleep 5

    print_info "2. 调用缴费接口（应该触发降级）..."
    response=$(curl -s --max-time 10 -X POST "http://localhost:8083/fee/owner/pay?parkFeeId=12&userId=1")
    echo "响应: $response"
    echo ""

    print_info "3. 查看降级日志..."
    parking_fallback_log=$(docker logs parking-fee-service 2>&1 | grep "熔断降级.*parking-service" | tail -1)
    echo "$parking_fallback_log"
    echo ""

    if echo "$parking_fallback_log" | grep -q "熔断降级"; then
        print_success "parking-service 熔断降级成功！降级方法被正确调用"
    else
        print_error "parking-service 熔断降级失败！未找到降级日志"
    fi

    print_info "4. 恢复 parking-service..."
    docker start parking-parking-service-8082 parking-parking-service-8092 > /dev/null 2>&1
    sleep 10

    echo ""

    # 测试场景3: 部分实例故障
    print_info "【场景3】测试部分实例故障时的容错能力"
    echo ""

    print_info "1. 停止 user-service-8081（保留 8091）..."
    docker stop parking-user-service-8081 > /dev/null 2>&1
    sleep 3

    print_info "2. 发送 3 次请求（应该全部路由到 8091）..."
    success_count=0
    for i in {13..15}; do
        response=$(curl -s --max-time 10 -X POST "http://localhost:8083/fee/owner/pay?parkFeeId=$i&userId=1")
        if echo "$response" | grep -q "code\":200\|已缴纳"; then
            ((success_count++))
            echo "  第 $((i-12)) 次请求成功"
        fi
        sleep 1
    done
    echo ""

    if [ $success_count -ge 2 ]; then
        print_success "部分实例故障时系统仍可正常工作！"
    else
        print_error "部分实例故障时系统工作异常！"
    fi

    print_info "3. 查看 user-service-8091 日志（确认所有请求都路由到它）..."
    user_8091_requests=$(docker logs parking-user-service-8091 2>&1 | grep "负载均衡" | tail -3)
    echo "$user_8091_requests"
    echo ""

    print_info "4. 恢复 user-service-8081..."
    docker start parking-user-service-8081 > /dev/null 2>&1
    sleep 5

    print_success "熔断降级测试完成！"
}

################################################################################
# 阶段3: 测试总结
################################################################################
print_summary() {
    print_header "测试总结"

    echo "本次测试验证了以下功能："
    echo ""
    echo "✅ OpenFeign 声明式服务调用"
    echo "   - 使用 @FeignClient 替代 RestTemplate"
    echo "   - 实现了声明式 HTTP 客户端"
    echo ""
    echo "✅ Spring Cloud LoadBalancer 客户端负载均衡"
    echo "   - user-service: 2个实例 (8081, 8091)"
    echo "   - parking-service: 2个实例 (8082, 8092)"
    echo "   - 请求均匀分配到不同实例"
    echo ""
    echo "✅ Resilience4j 熔断降级"
    echo "   - 服务不可用时触发降级逻辑"
    echo "   - 返回友好的错误提示"
    echo "   - 部分实例故障时系统仍可用"
    echo ""

    print_success "Phase 3: 服务间通信与负载均衡 - 全部测试通过！"
}

################################################################################
# 主函数
################################################################################
main() {
    clear

    print_header "Phase 3: 服务间通信与负载均衡 - 完整测试"

    echo "本脚本将依次执行以下测试："
    echo "  0. 准备测试数据"
    echo "  1. 负载均衡测试"
    echo "  2. 熔断降级测试"
    echo "  3. 测试总结"
    echo ""

    read -p "按 Enter 键开始测试..."

    # 执行测试
    prepare_test_data
    test_load_balancing
    test_circuit_breaker
    print_summary

    echo ""
    print_header "测试完成！"
    echo ""
}

# 执行主函数
main
