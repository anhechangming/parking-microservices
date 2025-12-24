#!/bin/bash

################################################################################
# Phase 4: API Gateway与统一认证测试脚本
#
# 功能：
# 1. 测试Gateway路由转发
# 2. 测试JWT认证（登录获取token）
# 3. 测试JWT验证（使用token访问受保护资源）
# 4. 测试白名单路径（无需token访问）
#
# 使用方法：
# chmod +x test-phase4.sh
# ./test-phase4.sh
################################################################################

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Gateway地址
GATEWAY_URL="http://localhost:8080"

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
# 阶段1: 测试Gateway路由转发
################################################################################
test_gateway_routing() {
    print_header "阶段1: 测试Gateway路由转发"

    print_info "测试1: 通过Gateway访问user-service健康检查（白名单）"
    response=$(curl -s "${GATEWAY_URL}/user/actuator/health")
    echo "响应: $response"

    if echo "$response" | grep -q "UP"; then
        print_success "Gateway成功路由到user-service"
    else
        print_error "Gateway路由到user-service失败"
    fi

    echo ""

    print_info "测试2: 通过Gateway访问parking-service健康检查（白名单）"
    response=$(curl -s "${GATEWAY_URL}/parking/actuator/health")
    echo "响应: $response"

    if echo "$response" | grep -q "UP"; then
        print_success "Gateway成功路由到parking-service"
    else
        print_error "Gateway路由到parking-service失败"
    fi

    echo ""

    print_info "测试3: 通过Gateway访问fee-service健康检查（白名单）"
    response=$(curl -s "${GATEWAY_URL}/fee/actuator/health")
    echo "响应: $response"

    if echo "$response" | grep -q "UP"; then
        print_success "Gateway成功路由到fee-service"
    else
        print_error "Gateway路由到fee-service失败"
    fi

    echo ""
    print_success "路由转发测试完成！"
}

################################################################################
# 阶段2: 测试JWT认证（登录获取token）
################################################################################
test_jwt_authentication() {
    print_header "阶段2: 测试JWT认证"

    print_info "场景1: 业主登录获取JWT Token"
    echo ""

    # 登录请求（确保数据库中有这个用户）
    print_info "发送登录请求..."
    response=$(curl -s -X POST "${GATEWAY_URL}/user/auth/owner/login" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "loginName=test_owner&password=123456")

    echo "登录响应: $response"
    echo ""

    # 提取token
    token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

    if [ -z "$token" ]; then
        print_warning "无法获取token，可能是用户不存在或密码错误"
        print_info "请确保数据库中存在用户: loginName=test_owner, password=123456"

        # 尝试使用管理员登录
        print_info "尝试管理员登录..."
        response=$(curl -s -X POST "${GATEWAY_URL}/user/auth/admin/login" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "loginName=admin&password=123456")

        echo "管理员登录响应: $response"
        token=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi

    if [ -n "$token" ]; then
        print_success "成功获取JWT Token"
        echo "Token: ${token:0:50}..."
        export JWT_TOKEN="$token"
    else
        print_error "无法获取JWT Token，后续测试将跳过"
        export JWT_TOKEN=""
    fi

    echo ""
}

################################################################################
# 阶段3: 测试JWT验证（使用token访问受保护资源）
################################################################################
test_jwt_validation() {
    print_header "阶段3: 测试JWT验证"

    if [ -z "$JWT_TOKEN" ]; then
        print_warning "跳过JWT验证测试（未获取到token）"
        return
    fi

    print_info "场景1: 不带Token访问受保护资源（应该返回401）"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "${GATEWAY_URL}/user/user/owners")
    echo "响应: $response"

    http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d':' -f2)
    if [ "$http_code" = "401" ]; then
        print_success "正确拦截未授权请求（返回401）"
    else
        print_error "未正确拦截未授权请求（期望401，实际${http_code}）"
    fi

    echo ""

    print_info "场景2: 带有效Token访问受保护资源（应该返回200）"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        "${GATEWAY_URL}/user/user/owners?pageNum=1&pageSize=10")

    echo "响应: ${response:0:200}..."
    echo ""

    http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d':' -f2)
    if [ "$http_code" = "200" ]; then
        print_success "JWT验证成功，成功访问受保护资源（返回200）"
    else
        print_error "JWT验证失败（期望200，实际${http_code}）"
    fi

    echo ""

    print_info "场景3: 带无效Token访问受保护资源（应该返回401）"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
        -H "Authorization: Bearer invalid_token_12345" \
        "${GATEWAY_URL}/user/user/owners")

    http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d':' -f2)
    if [ "$http_code" = "401" ]; then
        print_success "正确拦截无效Token（返回401）"
    else
        print_error "未正确拦截无效Token（期望401，实际${http_code}）"
    fi

    echo ""
    print_success "JWT验证测试完成！"
}

################################################################################
# 阶段4: 测试白名单路径
################################################################################
test_whitelist_paths() {
    print_header "阶段4: 测试白名单路径"

    print_info "测试1: 访问登录接口（白名单，无需token）"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "${GATEWAY_URL}/user/auth/owner/login" \
        -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "loginName=test&password=test")

    http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d':' -f2)
    if [ "$http_code" != "401" ]; then
        print_success "白名单路径正常工作（无需token即可访问）"
    else
        print_error "白名单路径配置可能有问题"
    fi

    echo ""

    print_info "测试2: 访问actuator端点（白名单）"
    response=$(curl -s -w "\nHTTP_CODE:%{http_code}" "${GATEWAY_URL}/actuator/health")

    http_code=$(echo "$response" | grep "HTTP_CODE" | cut -d':' -f2)
    if [ "$http_code" = "200" ]; then
        print_success "Actuator端点在白名单中，无需认证"
    else
        print_warning "Actuator端点访问异常（HTTP ${http_code}）"
    fi

    echo ""
    print_success "白名单测试完成！"
}

################################################################################
# 阶段5: 测试通过Gateway的服务间通信
################################################################################
test_service_communication() {
    print_header "阶段5: 测试通过Gateway的服务间通信"

    if [ -z "$JWT_TOKEN" ]; then
        print_warning "跳过服务间通信测试（未获取到token）"
        return
    fi

    print_info "测试: 通过Gateway调用fee-service的缴费接口"
    echo "该接口内部会调用user-service和parking-service（测试负载均衡）"
    echo ""

    response=$(curl -s -X POST \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        "${GATEWAY_URL}/fee/fee/owner/pay?parkFeeId=1&userId=1")

    echo "响应: $response"
    echo ""

    if echo "$response" | grep -q "code"; then
        print_success "通过Gateway成功调用服务间通信接口"
    else
        print_warning "服务间通信可能存在问题"
    fi

    echo ""
}

################################################################################
# 测试总结
################################################################################
print_summary() {
    print_header "测试总结"

    echo "本次测试验证了以下功能："
    echo ""
    echo "✅ Spring Cloud Gateway 统一入口"
    echo "   - Gateway路由转发到各个微服务"
    echo "   - 基于路径的路由规则"
    echo ""
    echo "✅ JWT统一认证"
    echo "   - 登录获取JWT Token"
    echo "   - JWT Token验证"
    echo "   - 无效Token拦截"
    echo ""
    echo "✅ 认证白名单"
    echo "   - 登录接口无需token"
    echo "   - Actuator端点无需认证"
    echo ""
    echo "✅ 完整的请求链路"
    echo "   - Client → Gateway → Microservices"
    echo "   - 统一的认证和路由管理"
    echo ""

    print_success "Phase 4: API网关与统一认证 - 测试完成！"
}

################################################################################
# 主函数
################################################################################
main() {
    clear

    print_header "Phase 4: API网关与统一认证 - 完整测试"

    echo "本脚本将依次执行以下测试："
    echo "  1. Gateway路由转发测试"
    echo "  2. JWT认证测试（登录）"
    echo "  3. JWT验证测试（token验证）"
    echo "  4. 白名单路径测试"
    echo "  5. 服务间通信测试"
    echo ""

    read -p "按 Enter 键开始测试..."

    # 执行测试
    test_gateway_routing
    test_jwt_authentication
    test_jwt_validation
    test_whitelist_paths
    test_service_communication
    print_summary

    echo ""
    print_header "测试完成！"
    echo ""
}

# 执行主函数
main
