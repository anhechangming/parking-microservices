# ============================================================================
# Phase 4: API网关与统一认证 - 完整部署与测试命令
# ============================================================================

# ============================================================================
# 一、本地打包JAR包
# ============================================================================

# 1. 清理并打包所有服务（跳过测试）
cd /d/桌面/PMS-\ Microservices/parking-microservices
mvn clean package -DskipTests

# 2. 单独打包gateway-service
cd gateway-service
mvn clean package -DskipTests
cd ..

# 3. 验证JAR包是否生成
ls -lh gateway-service/target/gateway-service.jar
ls -lh user-service/target/user-service.jar
ls -lh parking-service/target/parking-service.jar
ls -lh fee-service/target/fee-service.jar

# ============================================================================
# 二、上传JAR包到虚拟机（三种方式）
# ============================================================================

# 方式1: 使用scp上传
scp gateway-service/target/gateway-service.jar root@<虚拟机IP>:/root/parking-microservices/
scp user-service/target/user-service.jar root@<虚拟机IP>:/root/parking-microservices/
scp parking-service/target/parking-service.jar root@<虚拟机IP>:/root/parking-microservices/
scp fee-service/target/fee-service.jar root@<虚拟机IP>:/root/parking-microservices/
scp docker-compose.yml root@<虚拟机IP>:/root/parking-microservices/

# 方式2: 使用WinSCP图形界面上传（推荐）
# 打开WinSCP，连接到虚拟机，直接拖拽上传

# 方式3: 直接在虚拟机上拉取代码并打包
# ssh root@<虚拟机IP>
# git clone <your-repo-url>
# cd parking-microservices
# mvn clean package -DskipTests

# ============================================================================
# 三、在虚拟机上构建和启动Docker容器
# ============================================================================

# 1. SSH登录虚拟机
ssh root@<虚拟机IP>

# 2. 进入项目目录
cd /root/parking-microservices

# 3. 查看docker-compose版本
docker-compose --version

# 4. 构建所有服务镜像
docker-compose build

# 5. 启动所有服务（后台运行）
docker-compose up -d

# 6. 查看容器状态
docker-compose ps

# 7. 查看服务日志
docker-compose logs -f gateway-service    # 查看gateway日志
docker-compose logs -f user-service-1     # 查看用户服务日志
docker-compose logs --tail=100 gateway-service  # 查看最后100行日志

# 8. 等待所有服务启动（约1-2分钟）
sleep 60

# 9. 检查服务健康状态
docker ps | grep parking

# ============================================================================
# 四、验证服务注册情况
# ============================================================================

# 1. 访问Nacos控制台（在浏览器中）
# http://<虚拟机IP>:8848/nacos
# 用户名: nacos
# 密码: nacos

# 2. 通过命令行查询Nacos服务列表
curl -s "http://<虚拟机IP>:8848/nacos/v1/ns/instance/list?serviceName=gateway-service"
curl -s "http://<虚拟机IP>:8848/nacos/v1/ns/instance/list?serviceName=user-service"
curl -s "http://<虚拟机IP>:8848/nacos/v1/ns/instance/list?serviceName=parking-service"
curl -s "http://<虚拟机IP>:8848/nacos/v1/ns/instance/list?serviceName=fee-service"

# ============================================================================
# 五、测试API Gateway路由转发
# ============================================================================

# 定义Gateway地址（替换为你的虚拟机IP）
GATEWAY_URL="http://<虚拟机IP>:8080"

# 1. 测试Gateway健康检查
curl ${GATEWAY_URL}/actuator/health

# 2. 通过Gateway访问user-service
curl ${GATEWAY_URL}/user/actuator/health

# 3. 通过Gateway访问parking-service
curl ${GATEWAY_URL}/parking/actuator/health

# 4. 通过Gateway访问fee-service
curl ${GATEWAY_URL}/fee/actuator/health

# 5. 查看Gateway路由配置
curl ${GATEWAY_URL}/actuator/gateway/routes

# ============================================================================
# 六、测试JWT认证 - 登录获取Token
# ============================================================================

# 1. 业主登录（获取JWT Token）
# 注意：需要先确保数据库中有测试用户
curl -X POST "${GATEWAY_URL}/user/auth/owner/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "loginName=owner1&password=123456"

# 响应示例：
# {
#   "code": 200,
#   "message": "登录成功",
#   "data": {
#     "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWI...",
#     "userId": 1,
#     "username": "业主1",
#     "roleType": "owner"
#   }
# }

# 2. 管理员登录（获取JWT Token）
curl -X POST "${GATEWAY_URL}/user/auth/admin/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "loginName=admin&password=123456"

# 3. 保存Token到变量（从上面的响应中复制token值）
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMzY3ODQwMCwiZXhwIjoxNzAzNzY0ODAwfQ.abc..."

# ============================================================================
# 七、测试JWT验证 - 使用Token访问受保护接口
# ============================================================================

# 1. 不带Token访问受保护资源（应该返回401未授权）
curl -i ${GATEWAY_URL}/user/user/owners

# 预期响应：HTTP/1.1 401 Unauthorized

# 2. 带有效Token访问受保护资源（应该返回200）
curl -X GET "${GATEWAY_URL}/user/user/owners?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer ${TOKEN}"

# 3. 带无效Token访问（应该返回401）
curl -i -H "Authorization: Bearer invalid_token_123" \
  ${GATEWAY_URL}/user/user/owners

# 4. 访问业主详情
curl -H "Authorization: Bearer ${TOKEN}" \
  ${GATEWAY_URL}/user/user/owners/1

# 5. 查询停车位信息
curl -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/parking/parking/parkings?pageNum=1&pageSize=10"

# 6. 查询停车费信息
curl -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/fee/fee/park-fees?pageNum=1&pageSize=10"

# ============================================================================
# 八、测试完整业务流程（带JWT认证）
# ============================================================================

# 1. 登录获取Token
LOGIN_RESPONSE=$(curl -s -X POST "${GATEWAY_URL}/user/auth/owner/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "loginName=owner1&password=123456")

echo "登录响应: ${LOGIN_RESPONSE}"

# 2. 提取Token（使用jq工具，如果没有可以手动复制）
TOKEN=$(echo ${LOGIN_RESPONSE} | jq -r '.data.token')
echo "Token: ${TOKEN}"

# 3. 使用Token访问业主列表
curl -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/user/user/owners?pageNum=1&pageSize=10"

# 4. 使用Token查询停车位
curl -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/parking/parking/parkings?pageNum=1&pageSize=10"

# 5. 使用Token查询停车费
curl -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/fee/fee/park-fees?pageNum=1&pageSize=10"

# 6. 使用Token缴纳停车费（测试服务间调用）
curl -X POST -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/fee/fee/owner/pay?parkFeeId=1&userId=1"

# ============================================================================
# 九、测试负载均衡（通过Gateway）
# ============================================================================

# 1. 连续发送10次请求，观察负载分配
for i in {1..10}; do
  echo "请求 $i:"
  curl -s -H "Authorization: Bearer ${TOKEN}" \
    "${GATEWAY_URL}/user/user/owners/1" | head -c 100
  echo ""
  sleep 1
done

# 2. 查看两个user-service实例的日志，验证负载均衡
docker logs parking-user-service-8081 2>&1 | grep "负载均衡" | tail -10
docker logs parking-user-service-8091 2>&1 | grep "负载均衡" | tail -10

# 3. 统计负载分配
echo "user-service-8081 处理次数:"
docker logs parking-user-service-8081 2>&1 | grep -c "负载均衡"

echo "user-service-8091 处理次数:"
docker logs parking-user-service-8091 2>&1 | grep -c "负载均衡"

# ============================================================================
# 十、测试白名单路径（无需Token）
# ============================================================================

# 1. 访问登录接口（白名单）
curl ${GATEWAY_URL}/user/auth/owner/login \
  -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "loginName=test&password=test"

# 2. 访问健康检查（白名单）
curl ${GATEWAY_URL}/actuator/health

# 3. 访问Gateway路由信息（白名单）
curl ${GATEWAY_URL}/actuator/gateway/routes

# ============================================================================
# 十一、性能测试（使用ab工具）
# ============================================================================

# 1. 安装Apache Bench（如果没有）
# CentOS: yum install httpd-tools
# Ubuntu: apt-get install apache2-utils

# 2. 测试登录接口性能（100个请求，10个并发）
ab -n 100 -c 10 -p login.txt -T "application/x-www-form-urlencoded" \
  ${GATEWAY_URL}/user/auth/owner/login

# login.txt内容：
# loginName=owner1&password=123456

# 3. 测试受保护接口性能（需要先获取token）
ab -n 100 -c 10 -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/user/user/owners?pageNum=1&pageSize=10"

# ============================================================================
# 十二、Docker容器管理命令
# ============================================================================

# 1. 查看所有容器状态
docker-compose ps

# 2. 重启gateway服务
docker-compose restart gateway-service

# 3. 停止所有服务
docker-compose stop

# 4. 启动所有服务
docker-compose start

# 5. 停止并删除所有容器
docker-compose down

# 6. 停止并删除所有容器和数据卷
docker-compose down -v

# 7. 重新构建并启动
docker-compose up -d --build

# 8. 查看容器资源使用情况
docker stats

# 9. 进入容器内部
docker exec -it parking-gateway-service sh

# 10. 查看网络配置
docker network ls
docker network inspect parking-microservices_parking-network

# ============================================================================
# 十三、故障排查命令
# ============================================================================

# 1. 查看gateway-service日志
docker-compose logs -f gateway-service

# 2. 查看所有服务日志
docker-compose logs -f

# 3. 检查Nacos注册情况
curl "http://<虚拟机IP>:8848/nacos/v1/ns/instance/list?serviceName=gateway-service"

# 4. 检查网络连通性
docker exec parking-gateway-service ping parking-nacos
docker exec parking-gateway-service ping parking-user-service-8081

# 5. 查看容器内部进程
docker exec parking-gateway-service ps aux

# 6. 检查端口占用
netstat -tlnp | grep 8080

# 7. 查看Docker日志
journalctl -u docker -f

# ============================================================================
# 十四、数据库初始化（如果需要）
# ============================================================================

# 1. 进入user-db容器
docker exec -it user-db mysql -uroot -proot_password

# 2. 创建测试用户（在MySQL命令行中）
USE parking_user_db;

-- 创建测试业主（密码: 123456）
INSERT INTO owner (
  user_id, username, login_name, password,
  id_card, phone, status,
  create_time, update_time
) VALUES (
  1, '测试业主', 'owner1',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8itkXR6A9eL.0MmrWdwVGMvZKkDXa',
  '110101199001011234', '13800138000', '0',
  NOW(), NOW()
);

-- 创建测试管理员（密码: 123456）
INSERT INTO admin (
  user_id, username, login_name, password,
  phone, status,
  create_time, update_time
) VALUES (
  1, '系统管理员', 'admin',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8itkXR6A9eL.0MmrWdwVGMvZKkDXa',
  '13900139000', '0',
  NOW(), NOW()
);

# 3. 退出MySQL
exit;

# ============================================================================
# 十五、完整测试脚本
# ============================================================================

# 创建并执行测试脚本
cat > test-gateway.sh << 'EOF'
#!/bin/bash

# 配置
GATEWAY_URL="http://localhost:8080"

echo "========== Phase 4 测试开始 =========="

# 1. 登录获取Token
echo "1. 登录获取JWT Token..."
LOGIN_RESPONSE=$(curl -s -X POST "${GATEWAY_URL}/user/auth/owner/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "loginName=owner1&password=123456")

echo "登录响应: ${LOGIN_RESPONSE}"

# 提取Token
TOKEN=$(echo ${LOGIN_RESPONSE} | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，无法获取Token"
  exit 1
else
  echo "✅ 成功获取Token: ${TOKEN:0:50}..."
fi

# 2. 测试未授权访问
echo ""
echo "2. 测试未授权访问（无Token）..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${GATEWAY_URL}/user/user/owners")
if [ "$HTTP_CODE" = "401" ]; then
  echo "✅ 正确拦截未授权请求（返回401）"
else
  echo "❌ 未正确拦截（返回${HTTP_CODE}）"
fi

# 3. 测试授权访问
echo ""
echo "3. 测试授权访问（带Token）..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${TOKEN}" \
  "${GATEWAY_URL}/user/user/owners?pageNum=1&pageSize=10")
if [ "$HTTP_CODE" = "200" ]; then
  echo "✅ 授权访问成功（返回200）"
else
  echo "❌ 授权访问失败（返回${HTTP_CODE}）"
fi

# 4. 测试路由转发
echo ""
echo "4. 测试Gateway路由转发..."
services=("user" "parking" "fee")
for service in "${services[@]}"; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    "${GATEWAY_URL}/${service}/actuator/health")
  if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ ${service}-service 路由正常"
  else
    echo "❌ ${service}-service 路由异常（${HTTP_CODE}）"
  fi
done

echo ""
echo "========== Phase 4 测试完成 =========="
EOF

chmod +x test-gateway.sh
./test-gateway.sh

# ============================================================================
# 说明：
# 1. 将上述命令中的 <虚拟机IP> 替换为实际IP地址
# 2. 确保虚拟机防火墙开放了 8080, 8848, 3307-3309 端口
# 3. Token有效期为24小时，过期后需要重新登录
# 4. 所有命令都可以在虚拟机本地执行（localhost）或从外部执行（虚拟机IP）
# ============================================================================
