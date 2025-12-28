# 使用Nginx作为基础镜像
FROM nginx:alpine

# 删除默认的nginx配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/conf.d/

# 复制前端文件到nginx的html目录
COPY . /usr/share/nginx/html/

# 设置工作目录
WORKDIR /usr/share/nginx/html

# 修复权限问题（VirtualBox共享文件夹导致的）
RUN chmod -R 755 /usr/share/nginx/html && \
    find /usr/share/nginx/html -type f -exec chmod 644 {} \;

# 暴露80端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
