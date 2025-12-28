/**
 * 停车管理系统前端配置
 */

// API网关地址
const API_BASE_URL = 'http://localhost:9000';

// API端点
const API = {
    // 认证相关
    AUTH: {
        ADMIN_LOGIN: '/user/auth/admin/login',
        OWNER_LOGIN: '/user/auth/owner/login',
        LOGOUT: '/user/auth/logout'
    },

    // 业主管理（管理员）
    OWNERS: {
        LIST: '/user/user/owners',
        ALL: '/user/user/owners/all',
        GET: (id) => `/user/user/owners/${id}`,
        CREATE: '/user/user/owners',
        UPDATE: (id) => `/user/user/owners/${id}`,
        DELETE: (id) => `/user/user/owners/${id}`
    },

    // 车位管理（管理员）
    PARKINGS: {
        LIST: '/parking/parking/admin/parkings',
        AVAILABLE: '/parking/parking/admin/parkings/available',
        GET: (id) => `/parking/parking/admin/parkings/${id}`,
        CREATE: '/parking/parking/admin/parkings',
        UPDATE: (id) => `/parking/parking/admin/parkings/${id}`,
        DELETE: (id) => `/parking/parking/admin/parkings/${id}`,
        ASSIGN: '/parking/parking/admin/parkings/assign',
        RETURN: '/parking/parking/admin/parkings/return'
    },

    // 费用管理（管理员）
    FEES: {
        LIST: '/fee/fee/admin/list',
        GET: (id) => `/fee/fee/admin/${id}`,
        CREATE: '/fee/fee/admin',
        UPDATE: (id) => `/fee/fee/admin/${id}`,
        DELETE: (id) => `/fee/fee/admin/${id}`
    },

    // 业主端 - 车位
    OWNER_PARKING: {
        MY_PARKING: '/parking/parking/owner/my-parking',
        UPDATE_CAR: '/parking/parking/owner/update-car'
    },

    // 业主端 - 费用
    OWNER_FEES: {
        MY_FEES: '/fee/fee/owner/my-fees',
        UNPAID: '/fee/fee/owner/unpaid',
        PAID: '/fee/fee/owner/paid',
        ALL: '/fee/fee/owner/my-fees',
        PAY: '/fee/fee/owner/pay',
        GET: (id) => `/fee/fee/owner/${id}`
    }
};

// 数据字典
const DICT = {
    // 性别
    SEX: {
        '0': '男',
        '1': '女',
        '2': '未知'
    },

    // 用户状态
    USER_STATUS: {
        '0': '<span class="badge bg-success">正常</span>',
        '1': '<span class="badge bg-danger">停用</span>'
    },

    // 车位类型
    PARK_TYPE: {
        '0': '<span class="badge bg-primary">普通车位</span>',
        '1': '<span class="badge bg-success">充电车位</span>',
        '2': '<span class="badge bg-info">无障碍车位</span>'
    },

    // 车位状态
    PARK_STATUS: {
        '0': '<span class="badge bg-success">空闲</span>',
        '1': '<span class="badge bg-warning">已分配</span>'
    },

    // 缴费状态
    PAY_STATUS: {
        '0': '<span class="badge bg-danger">未缴费</span>',
        '1': '<span class="badge bg-success">已缴费</span>'
    }
};

// 分页默认配置
const PAGE_CONFIG = {
    DEFAULT_PAGE_NUM: 1,
    DEFAULT_PAGE_SIZE: 10,
    PAGE_SIZES: [10, 20, 50, 100]
};