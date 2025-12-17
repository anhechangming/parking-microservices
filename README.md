# 停车管理系统 - 微服务版拆分版  

基于Spring Boot 3.3.6 + Spring Cloud Alibaba + Nacos 3.1.0的微服务架构停车管理系统

## 📋 项目简介

本项目采用微服务架构设计，将停车管理系统拆分为3个独立的微服务，通过Nacos实现服务注册与发现，使用RestTemplate实现服务间调用。

### 服务拆分

| 服务名称 | 端口 | 职责 | 数据库 | 依赖服务 |
|---------|------|------|--------|----------|
| **user-service** | 8081 | 用户管理+认证 | parking_user_db | 无 |
| **parking-service** | 8082 | 车位和停车记录管理 | parking_business_db | user-service |
| **fee-service** | 8083 | 停车费用计算和管理 | parking_fee_db | user-service, parking-service |

### 核心特性

- ✅ 微服务架构（3个独立服务）
- ✅ 服务注册与发现 (Nacos 3.1.0)
- ✅ 客户端负载均衡 (Spring Cloud LoadBalancer)
- ✅ RestTemplate服务间调用
- ✅ 独立数据库 (每个服务独立MySQL容器)
- ✅ Docker容器化部署
- ✅ 环境变量配置支持
- ✅ 无循环依赖设计

---

## 🏗️ 系统架构

### 服务依赖关系图（单向依赖，无循环）

```
                    ┌──────────────────┐
                    │  user-service    │ ← 基础服务（不依赖任何服务）
                    │  (用户+认证)      │   - 用户管理 CRUD
                    │  端口: 8081       │   - 登录认证
                    │  DB: user_db      │   - JWT生成
                    └────────┬─────────┘
                             ↑
                             │ 被调用
              ┌──────────────┴──────────────┐
              │                             │
     ┌────────┴─────────┐         ┌────────┴─────────┐
     │ parking-service  │         │   fee-service    │
     │ (车位服务)        │         │   (费用服务)      │
     │ 端口: 8082        │         │   端口: 8083      │
     │ DB: parking_db    │         │   DB: fee_db      │
     └──────────────────┘         └────────┬─────────┘
              ↑                            │
              │                            │
              └────────────────────────────┘
                parking-service被fee-service调用

调用链路：
1. fee-service → user-service (获取用户类型、VIP折扣)
2. fee-service → parking-service (获取停车记录)
3. parking-service → user-service (验证用户是否存在)

✅ 没有循环依赖！
```

### 数据库架构

每个微服务使用独立的MySQL数据库容器：

- **user-db (3307)**: 存储用户和认证信息
  - `sys_user` - 管理员表
  - `live_user` - 业主表

- **parking-db (3308)**: 存储车位和停车记录
  - `parking_space` - 车位表
  - `owner_parking` - 停车记录表

- **fee-db (3309)**: 存储停车费用
  - `fee_park` - 停车费记录表

---

## 🚀 快速开始

### 方式一：Docker部署（推荐）

```bash
# 1. 编译打包
mvn clean package -DskipTests

# 2. 启动所有容器
docker-compose up -d

# 3. 查看启动日志
docker-compose logs -f

# 4. 访问Nacos控制台验证
# http://localhost:8848/nacos (nacos/nacos)
```

### 方式二：本地开发

#### 步骤1: 启动基础设施

```bash
# 启动数据库和Nacos
docker-compose up -d user-db parking-db fee-db nacos

# 等待Nacos启动完成（约30-60秒）
docker logs -f parking-nacos
```

#### 步骤2: 启动服务（按顺序）

**使用IDE (推荐)**:
1. user-service (必须先启动，被其他服务依赖)
2. parking-service
3. fee-service

**使用Maven**:
```bash
# 在根目录执行
mvn clean package -DskipTests

# 分别启动各服务
cd user-service && mvn spring-boot:run &
cd parking-service && mvn spring-boot:run &
cd fee-service && mvn spring-boot:run &
```

#### 步骤3: 验证服务注册

访问Nacos控制台：http://localhost:8848/nacos (账号/密码: nacos/nacos)

在"服务管理 → 服务列表"中，应该看到3个服务：
- ✅ user-service
- ✅ parking-service
- ✅ fee-service

---

## 📡 服务间调用说明

### 核心技术

**服务发现与负载均衡**:
- 使用 `@LoadBalanced` 注解的 RestTemplate
- 通过Nacos进行服务发现
- 使用服务名代替IP地址（如 `http://user-service`）

### 1. parking-service 调用 user-service

**场景**: 管理员分配车位时，验证用户是否存在

**代码位置**: `parking-service/src/main/java/com/parking/parking/service/ParkingService.java:90-99`

```java
// parking-service/client/UserServiceClient.java
@Service
public class UserServiceClient {

    @Autowired
    @LoadBalanced  // 关键：启用客户端负载均衡和服务发现
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service";  // 使用服务名

    /**
     * 调用user-service获取用户信息
     */
    public Map<String, Object> getOwnerById(Long userId) {
        String url = USER_SERVICE_URL + "/user/owners/" + userId;  // 注意：/user不是/users
        log.info("【跨服务调用】调用user-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                return result.getData();
            }
            return null;
        } catch (Exception e) {
            log.error("调用user-service失败: userId={}, error={}", userId, e.getMessage());
            return null;  // 返回null，让调用方决定如何处理
        }
    }

    /**
     * 检查用户是否存在
     */
    public boolean checkUserExists(Long userId) {
        return getOwnerById(userId) != null;
    }
}

// parking-service/service/ParkingService.java
@Service
public class ParkingService {

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 分配车位给业主
     */
    @Transactional
    public boolean assignParkingToOwner(Long userId, Long parkId, String carNumber) {
        // 【跨服务调用】验证用户是否存在
        if (!userServiceClient.checkUserExists(userId)) {
            throw new RuntimeException("用户不存在，无法分配车位");  // 强依赖：用户不存在则失败
        }

        // 验证车位是否存在且可用
        ParkingSpace parkingSpace = parkingSpaceMapper.findById(parkId);
        if (parkingSpace == null) {
            throw new RuntimeException("车位不存在");
        }
        if (!"0".equals(parkingSpace.getParkStatus())) {
            throw new RuntimeException("车位已被占用");
        }

        // 创建停车记录并分配车位
        OwnerParking ownerParking = new OwnerParking();
        ownerParking.setUserId(userId);
        ownerParking.setParkId(parkId);
        ownerParking.setCarNum(carNumber);
        ownerParking.setEntryTime(new Date());
        ownerParking.setPaymentStatus("1");

        ownerParkingMapper.insert(ownerParking);

        // 更新车位状态
        parkingSpace.setParkStatus("1");
        parkingSpaceMapper.update(parkingSpace);

        return true;
    }
}
```

**关键点**:
- RestTemplate使用 `@LoadBalanced` 注解，启用Nacos服务发现
- URL使用服务名 `http://user-service` 而非 `http://localhost:8081`
- 如果user-service不可用，分配车位操作会失败

### 2. fee-service 调用 user-service

**场景**: 缴费时获取用户信息，验证用户是否存在

**代码位置**: `fee-service/src/main/java/com/parking/fee/service/ParkingFeeService.java:122-127`

```java
// fee-service/client/UserServiceClient.java
@Service
public class UserServiceClient {

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://user-service";

    /**
     * 获取用户信息（验证用户存在，计算VIP折扣）
     */
    public Map<String, Object> getOwnerById(Long userId) {
        String url = USER_SERVICE_URL + "/user/owners/" + userId;
        log.info("【跨服务调用】调用user-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                log.info("成功获取用户信息: userId={}", userId);
                return result.getData();
            }
            return null;
        } catch (Exception e) {
            log.error("调用user-service失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否VIP用户
     */
    public boolean isVipUser(Long userId) {
        Map<String, Object> owner = getOwnerById(userId);
        if (owner != null) {
            String userType = (String) owner.get("userType");
            return "VIP".equalsIgnoreCase(userType);
        }
        return false;
    }
}
```

### 3. fee-service 调用 parking-service

**场景**: 缴费时验证用户是否有有效的停车记录，并验证车位ID匹配

**代码位置**: `fee-service/src/main/java/com/parking/fee/service/ParkingFeeService.java:143-155`

```java
// fee-service/client/ParkingServiceClient.java
@Service
public class ParkingServiceClient {

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    private static final String PARKING_SERVICE_URL = "http://parking-service";

    /**
     * 获取用户的停车记录（用于缴费验证）
     */
    public Map<String, Object> getUserParkingRecord(Long userId) {
        String url = PARKING_SERVICE_URL + "/parking/owner/record?userId=" + userId;
        log.info("【跨服务调用】调用parking-service: GET {}", url);

        try {
            ResponseEntity<Result<Map<String, Object>>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Result<Map<String, Object>>>() {}
            );

            Result<Map<String, Object>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                log.info("【跨服务调用成功】成功获取用户停车记录: userId={}", userId);
                return result.getData();
            } else {
                log.error("【跨服务调用失败】获取用户停车记录失败: {}",
                    result != null ? result.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("【跨服务调用异常】调用parking-service失败: userId={}, error={}",
                userId, e.getMessage());
            throw new RuntimeException("无法获取停车记录：" + e.getMessage());  // 不吞异常！
        }
    }
}

// fee-service/service/ParkingFeeService.java
@Service
public class ParkingFeeService {

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ParkingServiceClient parkingServiceClient;

    /**
     * 业主缴纳停车费
     */
    public boolean payParkingFee(Long parkFeeId, Long userId) {
        // 【跨服务调用1】验证用户是否存在
        var userInfo = userServiceClient.getOwnerById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在，无法缴费");  // 强依赖
        }

        // 查询费用记录
        ParkingFee parkingFee = parkingFeeMapper.findById(parkFeeId);
        if (parkingFee == null) {
            throw new RuntimeException("停车费记录不存在");
        }

        if (!parkingFee.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此记录");
        }

        if ("1".equals(parkingFee.getPayParkStatus())) {
            throw new RuntimeException("该停车费已缴纳");
        }

        // 【跨服务调用2 - 关键业务依赖】调用 parking-service 验证停车记录
        // 只有用户当前有停车位分配记录，才能缴纳停车费
        var parkingRecord = parkingServiceClient.getUserParkingRecord(userId);
        if (parkingRecord == null) {
            throw new RuntimeException("用户没有停车记录，无法缴费。请先分配车位。");  // 强依赖
        }

        // 验证费用记录的车位ID与停车记录的车位ID一致
        Long recordParkId = parkingRecord.get("parkId") != null ?
            Long.valueOf(parkingRecord.get("parkId").toString()) : null;
        if (recordParkId == null || !recordParkId.equals(parkingFee.getParkId())) {
            throw new RuntimeException("费用记录与停车记录不匹配");
        }

        // 所有验证通过，执行缴费
        parkingFee.setPayParkStatus("1");
        parkingFee.setPayTime(new Date());
        return parkingFeeMapper.update(parkingFee) > 0;
    }
}
```

**关键点**:
1. **强依赖设计**: 如果user-service或parking-service不可用，缴费会失败
2. **业务验证**: 验证费用记录的park_id与停车记录的park_id是否一致
3. **异常传播**: 不使用try-catch吞掉异常，让错误正确传播
4. **API专门设计**: parking-service提供了 `/parking/owner/record` 接口专门供fee-service调用

**对应的parking-service接口**:

```java
// parking-service/controller/OwnerParkingController.java:64-71
/**
 * 【供其他服务调用】根据用户ID获取停车记录
 * 用于fee-service在缴费前验证用户是否有有效停车记录
 */
@GetMapping("/record")
public Result<OwnerParking> getParkingRecordByUserId(@RequestParam Long userId) {
    OwnerParking ownerParking = parkingService.getOwnerParking(userId);
    if (ownerParking == null) {
        return Result.error("该用户没有停车记录");
    }
    return Result.success(ownerParking);
}
```

---

## 💡 为什么需要这些跨服务调用？

### 业务场景1: 分配车位时必须验证用户存在

**问题**: 如果不验证用户存在，可能会将车位分配给不存在的用户ID

**解决方案**: parking-service调用user-service验证用户

```
管理员分配车位 (userId=999, parkId=1)
  ↓
parking-service 调用 user-service查询用户999
  ↓
user-service返回: 用户不存在
  ↓
parking-service抛出异常: "用户不存在，无法分配车位"
  ↓
分配失败 ✅ 保证数据一致性
```

### 业务场景2: 缴费时必须验证停车记录

**问题**: 如果用户没有停车记录（没有车位），不应该能够缴费

**解决方案**: fee-service调用parking-service验证停车记录

```
用户缴费 (userId=1, parkFeeId=1)
  ↓
fee-service 调用 user-service验证用户存在 ✅
  ↓
fee-service 查询费用记录 (park_id=5)
  ↓
fee-service 调用 parking-service获取停车记录
  ↓
parking-service返回: park_id=5, 车牌京A12345
  ↓
fee-service验证: 费用的park_id(5) == 停车记录的park_id(5) ✅
  ↓
更新缴费状态 ✅ 业务逻辑正确
```

### 业务场景3: 如果依赖服务不可用

**情况1: user-service宕机，尝试分配车位**
```bash
curl -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=1&parkId=5'
# 结果: {"code":500,"message":"用户不存在，无法分配车位"}
# ✅ 正确：保护数据一致性
```

**情况2: parking-service宕机，尝试缴费**
```bash
curl -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1'
# 结果: {"code":500,"message":"无法获取停车记录：No instances available for parking-service"}
# ✅ 正确：不允许在停车记录不可验证的情况下缴费
```

这就是**真正的微服务依赖关系**，不是假接口！

---

## 🔗 完整业务流程示例

### 场景1：管理员分配车位给业主

```
1. 前端请求
   POST http://localhost:8082/parking/assign
   {
     "userId": 101,
     "parkId": 201
   }

2. parking-service 处理
   ↓
   调用 user-service 验证用户
   GET http://user-service:8081/users/owners/101
   ↓
3. user-service 返回用户信息
   {
     "userId": 101,
     "username": "张三",
     "userType": "VIP"
   }
   ↓
4. parking-service 分配车位
   - 检查车位是否可用
   - 创建停车记录
   - 更新车位状态
   ↓
5. 返回成功
```

### 场景2：业主缴纳停车费

```
1. 前端请求
   POST http://localhost:8083/fee/owner/pay
   {
     "feeId": 301,
     "userId": 101
   }

2. fee-service 处理
   ↓
   调用 user-service 获取用户类型
   GET http://user-service:8081/users/owners/101
   ↓
3. user-service 返回：userType="VIP"
   ↓
4. fee-service 调用 parking-service 获取停车记录
   GET http://parking-service:8082/parking/records/owner/101
   ↓
5. parking-service 返回停车天数
   ↓
6. fee-service 计算费用
   - 基础费用：30天 × 100元/天 = 3000元
   - VIP折扣：3000 × 0.8 = 2400元
   - 更新缴费状态
   ↓
7. 返回缴费成功
```

---

## 📚 API接口文档

### user-service (端口 8081)

#### 认证接口
```
POST /auth/admin/login - 管理员登录
POST /auth/owner/login - 业主登录
POST /auth/logout - 退出登录
```

#### 用户管理
```
GET /users/owners - 查询业主列表
GET /users/owners/{userId} - 查询业主详情 ✅ 被其他服务调用
POST /users/owners - 新增业主
PUT /users/owners/{userId} - 更新业主
DELETE /users/owners/{userId} - 删除业主
```

### parking-service (端口 8082)

#### 车位管理
```
GET /parking/spaces - 查询车位列表
GET /parking/spaces/{parkId} - 查询车位详情
POST /parking/spaces - 新增车位
PUT /parking/spaces/{parkId} - 更新车位
DELETE /parking/spaces/{parkId} - 删除车位
POST /parking/assign - 分配车位（调用user-service验证用户）
POST /parking/return - 退还车位
```

#### 停车记录
```
GET /parking/records - 查询停车记录列表
GET /parking/records/{recordId} - 查询停车记录详情 ✅ 被fee-service调用
GET /parking/records/owner/{userId} - 查询用户停车记录 ✅ 被fee-service调用
```

### fee-service (端口 8083)

#### 费用管理（管理员）
```
GET /fee/admin/list - 查询费用列表
GET /fee/admin/{feeId} - 查询费用详情
POST /fee/admin - 新增费用记录
PUT /fee/admin/{feeId} - 更新费用
DELETE /fee/admin/{feeId} - 删除费用
```

#### 费用查询（业主）
```
GET /fee/owner/my-fees - 查看我的费用（调用user-service获取用户信息）
GET /fee/owner/unpaid - 查看未缴费列表
POST /fee/owner/pay - 缴纳费用（调用user-service和parking-service）
GET /fee/owner/{feeId} - 查看费用详情
```

---

## 🔧 配置说明

### 端口映射

| 服务 | 容器端口 | 宿主机端口 | 访问地址 |
|------|---------|-----------|---------|
| user-db | 3306 | 3307 | localhost:3307 |
| parking-db | 3306 | 3308 | localhost:3308 |
| fee-db | 3306 | 3309 | localhost:3309 |
| nacos | 8848/9848 | 8848/9848 | http://localhost:8848/nacos |
| user-service | 8081 | 8081 | http://localhost:8081 |
| parking-service | 8082 | 8082 | http://localhost:8082 |
| fee-service | 8083 | 8083 | http://localhost:8083 |

### 数据库配置

```yaml
# 用户数据库
user-db:
  - 数据库名: parking_user_db
  - 用户: user_user / user_pass
  - Root密码: root_password
  - 端口: 3307

# 停车业务数据库
parking-db:
  - 数据库名: parking_business_db
  - 用户: parking_user / parking_pass
  - Root密码: root_password
  - 端口: 3308

# 费用数据库
fee-db:
  - 数据库名: parking_fee_db
  - 用户: fee_user / fee_pass
  - Root密码: root_password
  - 端口: 3309
```

---

## 🔄 微服务拆分策略

### 原始单体架构的问题

在拆分前，停车管理系统是一个单体应用，存在以下问题：
1. **代码耦合**: 所有功能在一个项目中，修改一处可能影响其他模块
2. **部署困难**: 修改一个小功能需要重新部署整个应用
3. **扩展性差**: 无法针对高负载模块单独扩展
4. **技术栈固定**: 所有模块必须使用相同的技术栈
5. **故障影响大**: 一个模块出错可能导致整个系统不可用

### 拆分原则

基于**领域驱动设计（DDD）**和**单一职责原则**，按照业务边界拆分：

#### 1. 识别核心业务领域

```
停车管理系统
  ├─ 用户域 (User Domain)
  │   └─ 用户管理、认证授权
  │
  ├─ 停车域 (Parking Domain)
  │   └─ 车位管理、停车记录
  │
  └─ 费用域 (Fee Domain)
      └─ 费用计算、缴费管理
```

#### 2. 定义服务边界

| 服务 | 职责 | 数据 | 依赖 |
|-----|------|------|------|
| **user-service** | 用户CRUD、登录认证、JWT生成 | 管理员表、业主表 | 无 |
| **parking-service** | 车位CRUD、分配车位、停车记录 | 车位表、停车记录表 | user-service |
| **fee-service** | 费用生成、费用查询、在线缴费 | 费用记录表 | user-service, parking-service |

#### 3. 数据库拆分

**原则**: 每个服务拥有独立的数据库，避免跨服务直接访问数据库

**实施**:
```
单体应用                      微服务架构
parking_db                   user-db (parking_user_db)
├─ sys_user        →         ├─ sys_user
├─ live_user       →         └─ live_user
├─ parking_space   →
├─ owner_parking   →         parking-db (parking_business_db)
├─ fee_park        →         ├─ parking_space
└─ ...             →         └─ owner_parking

                             fee-db (parking_fee_db)
                             └─ fee_park
```

#### 4. 服务间通信设计

**原则**: 避免循环依赖，采用单向依赖

**决策过程**:
```
问题1: parking-service分配车位时需要验证用户
方案: parking-service → user-service ✅

问题2: fee-service缴费时需要验证用户和停车记录
方案: fee-service → user-service + parking-service ✅

问题3: 是否需要user-service调用其他服务？
分析: 用户管理是基础服务，不依赖业务数据
结果: user-service不依赖任何服务 ✅
```

**最终依赖关系**:
```
user-service (基础层)
    ↑
    ├─ parking-service (业务层)
    │       ↑
    └─ fee-service (业务层)
            ↑
```

#### 5. API设计

**原则**:
- 对外API：面向终端用户和前端
- 内部API：专门供其他微服务调用，标注【供其他服务调用】

**示例**:
```java
// user-service: 对外API和内部API共用
@GetMapping("/user/owners/{userId}")  // 既可被前端调用，也可被其他服务调用
public Result<Owner> getOwnerById(@PathVariable Long userId) { ... }

// parking-service: 专门为fee-service设计的内部API
@GetMapping("/parking/owner/record")  // 【供其他服务调用】
public Result<OwnerParking> getParkingRecordByUserId(@RequestParam Long userId) { ... }
```

### 拆分后的收益

1. ✅ **独立部署**: 修改fee-service不影响user-service和parking-service
2. ✅ **独立扩展**: 可以只扩展高负载的服务（如fee-service）
3. ✅ **故障隔离**: 一个服务宕机不会导致整个系统不可用（降级处理）
4. ✅ **技术多样性**: 未来可以用不同语言实现不同服务
5. ✅ **团队独立**: 不同团队可以独立开发维护各自的服务

### 拆分的挑战与解决方案

| 挑战 | 解决方案 |
|-----|---------|
| **数据一致性** | 使用跨服务调用验证数据完整性 |
| **分布式事务** | 目前使用本地事务，未来可引入Seata |
| **服务发现** | 使用Nacos实现自动服务注册与发现 |
| **负载均衡** | 使用Spring Cloud LoadBalancer客户端负载均衡 |
| **配置管理** | 目前使用application.yml，未来可使用Nacos配置中心 |
| **链路追踪** | 建议引入Sleuth+Zipkin（待实现） |
| **API网关** | 建议引入Spring Cloud Gateway（待实现） |

---

## 🐛 常见问题

### 1. 服务启动顺序很重要吗？

是的！由于存在服务依赖，建议按以下顺序启动：

```bash
# 1. 基础设施
docker-compose up -d user-db parking-db fee-db nacos

# 2. 基础服务（不依赖其他服务）
docker-compose up -d user-service

# 3. 依赖user-service的服务
docker-compose up -d parking-service

# 4. 依赖其他服务的服务
docker-compose up -d fee-service

# 或者一次性启动（docker-compose会自动处理依赖）
docker-compose up -d
```

### 2. 如何验证服务间调用是否正常？

```bash
# 1. 查看Nacos控制台，确认所有服务已注册
http://localhost:8848/nacos

# 2. 测试分配车位（parking-service调用user-service）
curl -X POST "http://localhost:8082/parking/assign" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"parkId":101}'

# 3. 查看服务日志，应该看到调用记录
docker logs parking-parking-service | grep "调用user-service"
docker logs parking-fee-service | grep "调用"
```

### 3. 服务调用失败怎么办？

```bash
# 1. 检查Nacos中服务是否注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service

# 2. 检查网络连通性
docker exec parking-parking-service ping user-service

# 3. 查看服务日志
docker logs parking-parking-service
docker logs parking-user-service
```

---

## 📊 项目结构

```
parking-microservices/
├── user-service/                    # 用户+认证服务
│   ├── src/main/java/.../
│   │   ├── controller/
│   │   │   ├── AuthController.java  # 登录认证
│   │   │   └── OwnerController.java # 用户管理
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── OwnerService.java
│   │   ├── entity/
│   │   │   ├── Admin.java
│   │   │   └── Owner.java
│   │   ├── mapper/
│   │   ├── common/
│   │   │   └── JwtUtils.java        # JWT工具类
│   │   └── config/
│   │       └── RestTemplateConfig.java
│   ├── Dockerfile
│   └── pom.xml
│
├── parking-service/                 # 车位服务
│   ├── src/main/java/.../
│   │   ├── controller/
│   │   │   └── ParkingController.java
│   │   ├── service/
│   │   │   └── ParkingService.java
│   │   ├── client/
│   │   │   └── UserServiceClient.java  # 调用user-service
│   │   └── entity/
│   │       ├── ParkingSpace.java
│   │       └── OwnerParking.java
│   ├── Dockerfile
│   └── pom.xml
│
├── fee-service/                     # 费用服务
│   ├── src/main/java/.../
│   │   ├── controller/
│   │   │   ├── FeeController.java
│   │   │   └── OwnerFeeController.java
│   │   ├── service/
│   │   │   └── ParkingFeeService.java
│   │   ├── client/
│   │   │   ├── UserServiceClient.java     # 调用user-service
│   │   │   └── ParkingServiceClient.java  # 调用parking-service
│   │   └── entity/
│   │       └── ParkingFee.java
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml               # Docker编排配置
├── pom.xml                          # 父POM
└── README.md                        # 本文件
```

---

## 技术栈

- **Spring Boot**: 3.3.6
- **Spring Cloud**: 2023.0.3
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Nacos**: v3.1.0
- **MyBatis**: 3.0.3
- **MySQL**: 8.4
- **JWT**: 0.12.6
- **JDK**: 17

---

## 🖥️ 虚拟机部署指南

### 前置要求

- Linux虚拟机 (CentOS/Ubuntu均可)
- Docker 20.10+
- Docker Compose 2.0+
- 至少4GB内存，2核CPU

### 完整部署流程

#### 步骤1: 在本地主机打包项目

```bash
# 在项目根目录执行
cd D:\桌面\PMS- Microservices\parking-microservices

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 打包完成后，会在各个服务的target目录生成jar包：
# - user-service/target/user-service-0.0.1-SNAPSHOT.jar
# - parking-service/target/parking-service-0.0.1-SNAPSHOT.jar
# - fee-service/target/fee-service-0.0.1-SNAPSHOT.jar
```

#### 步骤2: 传输文件到虚拟机

```bash
# 方式一：使用scp（在Windows主机上）
scp -r D:\桌面\PMS- Microservices\parking-microservices user@虚拟机IP:/home/user/

# 方式二：使用WinSCP或FileZilla等图形化工具
# 将整个parking-microservices文件夹传输到虚拟机
```

#### 步骤3: 在虚拟机上启动服务

```bash
# SSH连接到虚拟机
ssh user@虚拟机IP

# 进入项目目录
cd /home/user/parking-microservices

# 确保docker-compose.yml有执行权限
chmod +x test-microservices.sh

# 启动所有服务（首次启动会拉取镜像，需要等待）
docker compose up -d

# 查看启动日志
docker compose logs -f

# 等待所有服务健康检查通过（约1-2分钟）
docker compose ps
```

#### 步骤4: 验证服务注册

```bash
# 在虚拟机上执行
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=parking-service
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=fee-service

# 或访问Nacos控制台（从主机浏览器）
# http://虚拟机IP:8848/nacos (账号: nacos, 密码: nacos)
```

#### 步骤5: 运行测试脚本

```bash
# 在虚拟机上执行测试脚本
bash test-microservices.sh

# 该脚本会自动测试：
# 1. 各服务独立API调用
# 2. parking-service → user-service 跨服务调用
# 3. fee-service → user-service 跨服务调用
# 4. fee-service → parking-service 跨服务调用
# 5. 验证Nacos服务注册
```

### 常见虚拟机部署问题

#### 问题1: Docker镜像构建缓存问题

**现象**: 修改代码后重新打包，但虚拟机运行的还是旧代码

**原因**: Docker使用了缓存的镜像层

**解决方案**:
```bash
# 停止所有容器
docker compose down

# 清理旧镜像（可选）
docker rmi parking-user-service parking-parking-service parking-fee-service

# 不使用缓存重新构建
docker compose build --no-cache

# 启动服务
docker compose up -d
```

#### 问题2: 端口被占用

**现象**: `bind: address already in use`

**解决方案**:
```bash
# 查看端口占用
netstat -tulpn | grep -E '8081|8082|8083|8848|3307|3308|3309'

# 停止占用端口的进程
kill -9 <PID>

# 或修改docker-compose.yml中的端口映射
```

#### 问题3: 内存不足

**现象**: 服务启动后频繁重启或OOM

**解决方案**:
```bash
# 检查内存使用
free -h
docker stats

# 修改docker-compose.yml，限制每个服务的内存
services:
  user-service:
    deploy:
      resources:
        limits:
          memory: 512M
```

---

## 🧪 测试指南

nacos控制中心
![image-20251217234333861](images/image-20251217234333861.png)

数据库脚本导入

![image-20251217234352246](images/image-20251217234352246.png)

![image-20251217234402927](images/image-20251217234402927.png)

微服务跨服务调用测试脚本

![image-20251217234421623](images/image-20251217234421623.png)

![image-20251217234428301](images/image-20251217234428301.png)

![image-20251217234435498](images/image-20251217234435498.png)

![image-20251217234445367](images/image-20251217234445367.png)

fee-service测试 停止服务后测试

![image-20251217234510812](images/image-20251217234510812.png)

![image-20251217234518178](images/image-20251217234518178.png)

### 自动化测试

项目提供了完整的测试脚本 `test-microservices.sh`，涵盖所有跨服务调用场景。

#### 运行测试脚本

```bash
# 赋予执行权限
chmod +x test-microservices.sh

# 执行测试
bash test-microservices.sh
```

#### 测试内容说明

**测试1: 查询所有用户（user-service独立调用）**
```bash
curl -X GET 'http://localhost:8081/user/owners'
# 验证user-service基本功能正常
```

**测试2: 查询单个用户（会被其他服务调用）**
```bash
curl -X GET 'http://localhost:8081/user/owners/1'
# 验证跨服务调用的基础接口
```

**测试3-4: 停车位查询（parking-service独立调用）**
```bash
curl -X GET 'http://localhost:8082/parking/admin/parkings?pageNum=1&pageSize=10'
curl -X GET 'http://localhost:8082/parking/admin/parkings/available'
```

**【跨服务测试1】parking-service → user-service**
```bash
# 场景：分配车位时，parking-service调用user-service验证用户是否存在
curl -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=3&parkId=5&carNumber=京A99999'

# 预期行为：
# 1. parking-service接收请求
# 2. 调用user-service的 /user/owners/3 验证用户存在
# 3. 如果user-service返回用户信息，则分配车位
# 4. 如果user-service不可用或用户不存在，则失败

# 验证日志：
docker logs parking-parking-service 2>&1 | grep "调用user-service"
```

**【跨服务测试2】fee-service → user-service**
```bash
# 场景：查询费用时，fee-service调用user-service获取用户类型（VIP/NORMAL）
curl -X GET 'http://localhost:8083/fee/owner/my-fees?userId=2'

# 预期行为：
# 1. fee-service接收请求
# 2. 调用user-service获取用户类型
# 3. 根据VIP状态应用不同的业务逻辑（如折扣）

# 验证日志：
docker logs parking-fee-service 2>&1 | grep "调用user-service"
```

**【跨服务测试3】fee-service → user-service + parking-service**
```bash
# 场景：缴费时，fee-service同时调用两个服务
curl -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1'

# 预期行为：
# 1. fee-service接收缴费请求
# 2. 调用user-service验证用户是否存在
# 3. 调用parking-service获取用户的停车记录
# 4. 验证费用记录的车位ID与停车记录的车位ID是否一致
# 5. 所有验证通过后，更新缴费状态

# 验证日志：
docker logs parking-fee-service 2>&1 | grep "跨服务调用"
```

### 验证强依赖关系

#### 测试服务不可用场景

```bash
# 测试1: 停止parking-service，尝试缴费（应该失败）
docker stop parking-parking-service

curl -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1'
# 预期结果：{"code":500,"message":"无法获取停车记录：No instances available for parking-service"}

# 重新启动
docker start parking-parking-service

# 测试2: 停止user-service，尝试分配车位（应该失败）
docker stop parking-user-service

curl -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=3&parkId=5&carNumber=京A99999'
# 预期结果：{"code":500,"message":"用户不存在，无法分配车位"}

# 重新启动
docker start parking-user-service
```

这些测试证明了服务之间的**真正依赖关系**：
- ✅ fee-service **依赖** user-service和parking-service
- ✅ parking-service **依赖** user-service
- ✅ 如果依赖的服务不可用，调用会失败（不是假接口）

### 手动测试场景

#### 场景1: 完整的车位分配流程

```bash
# 1. 查询可用车位
curl -X GET 'http://localhost:8082/parking/admin/parkings/available'

# 2. 查询用户列表
curl -X GET 'http://localhost:8081/user/owners'

# 3. 分配车位（触发跨服务调用）
curl -X POST 'http://localhost:8082/parking/admin/parkings/assign?userId=1&parkId=1&carNumber=京A12345'

# 4. 验证分配结果
curl -X GET 'http://localhost:8082/parking/owner/my-parking?userId=1'
```

#### 场景2: 完整的缴费流程

```bash
# 1. 查询用户未缴费列表
curl -X GET 'http://localhost:8083/fee/owner/unpaid-fees?userId=1'

# 2. 缴纳停车费（触发多个跨服务调用）
curl -X POST 'http://localhost:8083/fee/owner/pay?parkFeeId=1&userId=1'

# 3. 查看缴费后的费用列表
curl -X GET 'http://localhost:8083/fee/owner/my-fees?userId=1'
```

---

## 🗄️ 数据库设计详解

### user-db (parking_user_db)

#### 表1: sys_user (管理员表)

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| user_id | BIGINT | 管理员ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(100) | 密码（加密） | 非空 |
| real_name | VARCHAR(50) | 真实姓名 | |
| phone | VARCHAR(20) | 手机号 | |
| status | CHAR(1) | 状态（0:正常 1:禁用） | 默认'0' |
| create_time | DATETIME | 创建时间 | |

#### 表2: live_user (业主表)

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| user_id | BIGINT | 业主ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(100) | 密码（加密） | 非空 |
| real_name | VARCHAR(50) | 真实姓名 | |
| phone | VARCHAR(20) | 手机号 | |
| user_type | VARCHAR(20) | 用户类型（VIP/NORMAL） | 默认'NORMAL' |
| status | CHAR(1) | 状态（0:正常 1:禁用） | 默认'0' |
| create_time | DATETIME | 创建时间 | |

**业务说明**:
- `user_type`字段用于区分VIP和普通用户，影响费用计算时的折扣
- 该表的数据会被parking-service和fee-service跨服务查询

### parking-db (parking_business_db)

#### 表1: parking_space (车位表)

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| park_id | BIGINT | 车位ID | 主键，自增 |
| park_num | VARCHAR(20) | 车位编号 | 唯一，如"A-101" |
| park_type | CHAR(1) | 车位类型（0:普通 1:充电 2:无障碍） | 默认'0' |
| park_status | CHAR(1) | 车位状态（0:空闲 1:已分配） | 默认'0' |
| create_time | DATETIME | 创建时间 | |

#### 表2: owner_parking (停车记录表)

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| record_id | BIGINT | 记录ID | 主键，自增 |
| user_id | BIGINT | 业主ID | 外键（逻辑） |
| park_id | BIGINT | 车位ID | 外键 |
| car_num | VARCHAR(20) | 车牌号 | 如"京A12345" |
| entry_time | DATETIME | 入场时间 | |
| exit_time | DATETIME | 出场时间 | 可为空 |
| parking_days | INT | 停车天数 | |
| parking_fee | DECIMAL(10,2) | 停车费用 | |
| payment_status | CHAR(1) | 缴费状态（0:未缴 1:已缴） | 默认'0' |

**业务说明**:
- `owner_parking`表记录用户当前的停车信息
- fee-service在缴费时会跨服务调用parking-service查询该表，验证用户是否有有效停车记录
- `park_id`字段必须与fee_park表的park_id匹配，否则缴费失败

### fee-db (parking_fee_db)

#### 表: fee_park (停车费记录表)

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| fee_id | BIGINT | 费用ID | 主键，自增 |
| user_id | BIGINT | 业主ID | 外键（逻辑） |
| park_id | BIGINT | 车位ID | 外键（逻辑） |
| pay_park_month | VARCHAR(10) | 缴费月份 | 如"2025-12" |
| pay_park_money | DECIMAL(10,2) | 应缴金额 | |
| pay_park_status | CHAR(1) | 缴费状态（0:未缴 1:已缴） | 默认'0' |
| pay_time | DATETIME | 缴费时间 | 可为空 |
| remark | VARCHAR(200) | 备注 | |

**业务说明**:
- 每个月为每个停车用户生成一条费用记录
- 缴费时需要跨服务验证：
  1. 用户是否存在（调用user-service）
  2. 用户是否有该车位的停车记录（调用parking-service）
  3. 费用记录的park_id与停车记录的park_id是否一致

### 数据库关系说明

```
┌─────────────────────────────────────────────────────────────┐
│                    跨服务数据关系                              │
└─────────────────────────────────────────────────────────────┘

user-db.live_user.user_id (业主ID)
    ↓ 被引用
    ├─→ parking-db.owner_parking.user_id (停车记录)
    └─→ fee-db.fee_park.user_id (费用记录)

parking-db.parking_space.park_id (车位ID)
    ↓ 被引用
    ├─→ parking-db.owner_parking.park_id (停车记录)
    └─→ fee-db.fee_park.park_id (费用记录)

业务约束：
- fee_park.park_id 必须等于 owner_parking.park_id (同一个用户)
- 这个约束通过跨服务调用在应用层实现
```

---

## 🔍 故障排查指南

### 1. 数据库连接问题

#### 问题: `Public Key Retrieval is not allowed`

**原因**: MySQL 8.x使用caching_sha2_password认证，需要允许公钥检索

**解决方案**:
```yaml
# 确保docker-compose.yml中的数据库URL包含以下参数
SPRING_DATASOURCE_URL: jdbc:mysql://user-db:3306/parking_user_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

#### 问题: `Unknown database 'parking_xxx_db'`

**原因**: 数据库未初始化

**解决方案**:
```bash
# 检查init.sql是否正确挂载
docker exec -it parking-user-db mysql -uroot -proot_password

# 手动创建数据库
CREATE DATABASE IF NOT EXISTS parking_user_db;
```

### 2. MyBatis映射问题

#### 问题: `Table 'xxx' doesn't exist`

**原因**: 代码中的表名与数据库实际表名不一致

**排查步骤**:
```bash
# 1. 连接数据库查看实际表名
docker exec -it parking-parking-db mysql -uparking_user -pparking_pass parking_business_db

mysql> SHOW TABLES;
mysql> DESC parking_space;

# 2. 检查Mapper中的SQL语句
# 确保@Select/@Insert/@Update中的表名与数据库一致
```

#### 问题: `Unknown column 'xxx' in 'field list'`

**原因**: Mapper中的字段名与数据库列名不匹配

**解决方案**:
```java
// 方式1: 使用别名
@Select("SELECT fee_id as feeId, pay_time as payTime FROM fee_park WHERE fee_id = #{feeId}")

// 方式2: 修改entity字段名匹配数据库（推荐）
// 数据库列: fee_id, pay_time
// Entity字段: feeId, payTime (MyBatis自动驼峰转换)
```

### 3. 跨服务调用问题

#### 问题: `No instances available for xxx-service`

**原因**: 目标服务未在Nacos注册或已下线

**排查步骤**:
```bash
# 1. 检查Nacos服务列表
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service

# 2. 检查目标服务是否运行
docker ps | grep user-service

# 3. 查看目标服务日志
docker logs parking-user-service | grep "Nacos registry"
```

#### 问题: 服务调用返回404

**原因**: URL路径错误

**常见错误**:
```java
// ❌ 错误：多了一个's'
String url = "http://user-service/users/owners/" + userId;

// ✅ 正确
String url = "http://user-service/user/owners/" + userId;
```

**排查方法**:
```bash
# 直接调用目标服务验证路径
curl http://localhost:8081/user/owners/1
```

#### 问题: 服务调用超时

**原因**: 网络问题或目标服务响应慢

**解决方案**:
```java
// 配置RestTemplate超时时间
@Bean
@LoadBalanced
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .build();
}
```

### 4. Spring Boot版本兼容性问题

#### 问题: `Spring Boot [3.5.7] is not compatible with this Spring Cloud release train`

**原因**: Spring Cloud版本与Spring Boot版本不兼容

**解决方案**:
```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <!-- 使用3.3.6，与Spring Cloud 2023.0.3兼容 -->
    <version>3.3.6</version>
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 5. Docker健康检查失败

#### 问题: 容器频繁重启，日志显示`unhealthy`

**原因**: MySQL 8.4移除了`default-authentication-plugin`参数

**解决方案**:
```yaml
# docker-compose.yml
services:
  user-db:
    # ❌ 删除此行（MySQL 8.4不支持）
    # command: --default-authentication-plugin=mysql_native_password

    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot_password"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 40s  # 给足启动时间
```

---

## 📝 总结

本项目成功将单体停车管理系统拆分为3个微服务，实现了：

### 技术架构
- ✅ 3个独立的Spring Boot微服务
- ✅ Nacos服务注册与发现
- ✅ RestTemplate + @LoadBalanced 服务间调用
- ✅ 每个服务独立的MySQL数据库
- ✅ Docker容器化部署

### 微服务特性
- ✅ 单向依赖设计，无循环依赖
- ✅ 真实的跨服务调用（强依赖）
- ✅ 服务不可用时正确失败（不是假接口）
- ✅ 业务数据完整性验证

### 部署与测试
- ✅ 完整的虚拟机部署流程
- ✅ 自动化测试脚本
- ✅ 详细的故障排查指南
- ✅ 数据库schema文档

### 下一步优化建议

1. **API网关**: 引入Spring Cloud Gateway统一入口
2. **配置中心**: 使用Nacos Config统一管理配置
3. **分布式事务**: 引入Seata处理跨服务事务
4. **链路追踪**: 引入Sleuth+Zipkin追踪请求链路
5. **熔断降级**: 引入Sentinel实现服务保护
6. **消息队列**: 引入RabbitMQ/Kafka实现异步通信
7. **监控告警**: 引入Prometheus+Grafana监控服务指标

---

## 📚 参考资料

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/github-pages/2023/zh-cn/index.html)
- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [MyBatis官方文档](https://mybatis.org/mybatis-3/zh/index.html)

---

## 许可证

本项目仅供学习使用。
