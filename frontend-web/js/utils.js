/**
 * 停车管理系统 - 工具类
 */

// ============ 认证管理 ============

const Auth = {
    /**
     * 保存登录信息
     */
    saveLoginInfo(token, userId, username, role) {
        localStorage.setItem('token', token);
        localStorage.setItem('userId', userId);
        localStorage.setItem('username', username);
        localStorage.setItem('role', role); // admin 或 owner
    },

    /**
     * 获取Token
     */
    getToken() {
        return localStorage.getItem('token');
    },

    /**
     * 获取用户ID
     */
    getUserId() {
        return localStorage.getItem('userId');
    },

    /**
     * 获取用户名
     */
    getUsername() {
        return localStorage.getItem('username');
    },

    /**
     * 获取角色
     */
    getRole() {
        return localStorage.getItem('role');
    },

    /**
     * 检查是否已登录
     */
    isLoggedIn() {
        return !!this.getToken();
    },

    /**
     * 清除登录信息
     */
    clearLoginInfo() {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
    },

    /**
     * 退出登录
     */
    logout() {
        this.clearLoginInfo();
        window.location.href = '/index.html';
    },

    /**
     * 检查权限（页面加载时调用）
     */
    checkAuth(requiredRole) {
        if (!this.isLoggedIn()) {
            alert('请先登录！');
            window.location.href = '/index.html';
            return false;
        }

        if (requiredRole && this.getRole() !== requiredRole) {
            alert('无权访问此页面！');
            window.location.href = '/index.html';
            return false;
        }

        return true;
    }
};

// ============ HTTP请求封装 ============

// 创建axios实例
const http = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000
});

// 请求拦截器
http.interceptors.request.use(
    config => {
        const token = Auth.getToken();
        if (token) {
            // 如果token不是以Bearer开头，自动添加
            config.headers.Authorization = token.startsWith('Bearer ') ? token : 'Bearer ' + token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
http.interceptors.response.use(
    response => {
        const res = response.data;

        // 如果code不是200，统一处理错误
        if (res.code !== 200) {
            Utils.showError(res.message || '请求失败');
            return Promise.reject(new Error(res.message || '请求失败'));
        }

        return res;
    },
    error => {
        console.error('请求错误：', error);

        if (error.response) {
            switch (error.response.status) {
                case 401:
                    Utils.showError('登录已过期，请重新登录');
                    Auth.logout();
                    break;
                case 403:
                    Utils.showError('无权访问');
                    break;
                case 404:
                    Utils.showError('请求的资源不存在');
                    break;
                case 500:
                    Utils.showError('服务器错误');
                    break;
                default:
                    Utils.showError(error.response.data?.message || '请求失败');
            }
        } else if (error.request) {
            Utils.showError('网络错误，请检查网络连接');
        } else {
            Utils.showError('请求配置错误');
        }

        return Promise.reject(error);
    }
);

// ============ 工具函数 ============

const Utils = {
    /**
     * 显示成功消息
     */
    showSuccess(message) {
        // 使用Bootstrap的Toast组件
        this.showToast(message, 'success');
    },

    /**
     * 显示错误消息
     */
    showError(message) {
        this.showToast(message, 'danger');
    },

    /**
     * 显示提示消息
     */
    showInfo(message) {
        this.showToast(message, 'info');
    },

    /**
     * 显示Toast消息
     */
    showToast(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            console.error('Toast容器不存在');
            alert(message);
            return;
        }

        const toastHtml = `
            <div class="toast align-items-center text-white bg-${type} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;

        const toastElement = $(toastHtml).appendTo(toastContainer);
        const toast = new bootstrap.Toast(toastElement[0], { delay: 3000 });
        toast.show();

        // 移除DOM
        toastElement.on('hidden.bs.toast', function() {
            $(this).remove();
        });
    },

    /**
     * 确认对话框
     */
    confirm(message, callback) {
        if (confirm(message)) {
            callback();
        }
    },

    /**
     * 格式化日期时间
     */
    formatDateTime(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },

    /**
     * 格式化日期
     */
    formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN');
    },

    /**
     * 格式化金额
     */
    formatMoney(amount) {
        if (amount === null || amount === undefined) return '-';
        return '¥' + parseFloat(amount).toFixed(2);
    },

    /**
     * 获取URL参数
     */
    getUrlParam(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    },

    /**
     * 构建分页HTML
     */
    buildPagination(current, total, pageSize, onPageChange) {
        const totalPages = Math.ceil(total / pageSize);

        if (totalPages <= 1) {
            return '';
        }

        let html = '<nav><ul class="pagination justify-content-center">';

        // 上一页
        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current - 1}">上一页</a>
        </li>`;

        // 页码
        const showPages = 5; // 显示5个页码
        let startPage = Math.max(1, current - Math.floor(showPages / 2));
        let endPage = Math.min(totalPages, startPage + showPages - 1);

        if (endPage - startPage + 1 < showPages) {
            startPage = Math.max(1, endPage - showPages + 1);
        }

        // 首页
        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" data-page="1">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        // 页码按钮
        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>`;
        }

        // 尾页
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">${totalPages}</a></li>`;
        }

        // 下一页
        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current + 1}">下一页</a>
        </li>`;

        html += '</ul></nav>';

        // 绑定点击事件
        setTimeout(() => {
            $('.pagination a').click(function(e) {
                e.preventDefault();
                const page = parseInt($(this).data('page'));
                if (page && !$(this).parent().hasClass('disabled') && !$(this).parent().hasClass('active')) {
                    onPageChange(page);
                }
            });
        }, 0);

        return html;
    },

    /**
     * 空数据提示
     */
    emptyData(colspan) {
        return `<tr><td colspan="${colspan}" class="text-center text-muted py-4">暂无数据</td></tr>`;
    }
};

// ============ 表单验证 ============

const Validator = {
    /**
     * 验证手机号
     */
    isPhone(phone) {
        return /^1[3-9]\d{9}$/.test(phone);
    },

    /**
     * 验证身份证号
     */
    isIdCard(idCard) {
        return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard);
    },

    /**
     * 验证密码（6-20位）
     */
    isPassword(password) {
        return password && password.length >= 6 && password.length <= 20;
    },

    /**
     * 验证车牌号
     */
    isCarNumber(carNumber) {
        return /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4,5}[A-Z0-9挂学警港澳]{1}$/.test(carNumber);
    }
};
