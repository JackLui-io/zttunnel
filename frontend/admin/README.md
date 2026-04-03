# 隧道管理系统 (zt_tunnel_management)

## 项目简介

隧道管理系统是一个基于 Vue 3 + Element Plus 的现代化管理系统，用于管理隧道照明设备、监控能耗数据、分析碳排放等功能。

本项目对接 zttunnel/zt_project_tunnel 后端服务。

## 技术栈

- **前端框架**: Vue 3
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **构建工具**: Vite
- **样式预处理**: Sass
- **图表库**: ECharts

## 功能模块

### 系统管理
- 用户管理：用户增删改查、权限分配
- 角色管理：角色权限配置
- 菜单管理：系统菜单配置
- 部门管理：组织架构管理

### 隧道管理
- 隧道列表：隧道基础信息管理
- 隧道层级：四级层级结构管理

### 设备管理
- 设备列表：设备基础信息管理
- 终端管理：边缘计算终端管理
- 灯具管理：灯具设备管理
- 雷达管理：雷达设备管理

### 电能管理
- 电能监测：实时电能数据监控
- 电表配置：电表参数配置
- 厂商配置：电表厂商信息管理

### 能碳管理
- 能碳数据：能耗和碳排放数据
- 统计分析：数据统计和趋势分析

### 监控管理
- 实时监控：设备实时状态监控
- 车流监控：隧道车流量监控
- 亮度监控：隧道亮度监控

### 通知管理
- 通知列表：系统通知管理
- 报警通知：设备报警信息

### 日志管理
- 操作日志：用户操作记录
- 登录日志：用户登录记录

## 项目结构

```
zt_tunnel_management/
├── public/                 # 静态资源
├── src/
│   ├── api/              # API 接口（对接 zttunnel 后端）
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── styles/           # 全局样式
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── package.json          # 项目配置
├── vite.config.js        # Vite 配置
└── README.md            # 项目说明
```

## API 对接说明

本项目的 API 接口已对接 zttunnel/zt_project_tunnel 后端服务：

### 已对接的接口
- 登录认证：/login, /getInfo, /logout
- 隧道管理：/tunnel/*
- 设备管理：/device/*
- 分析统计：/analyze/*
- 通知管理：/notice/*
- 系统管理：/system/*
- 监控日志：/monitor/*

### 新增的后端接口
以下接口需要在后端新增（已创建对应的 Controller）：
- 设备告警：/device/alarm/*
- 设备参数：/tunnelDeviceParam/saveOrUpdate, /tunnelDeviceParam/{id}

## 开发指南

### 环境要求

- Node.js >= 16
- npm >= 8

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 后端服务

本项目需要配合 zttunnel/zt_project_tunnel 后端服务使用：

1. 启动后端服务（默认端口 8080）
2. 前端开发服务器会自动代理 /api 请求到后端

## 部署说明

### 前端部署
1. 执行 `npm run build` 构建项目
2. 将 `dist` 目录部署到 Web 服务器
3. 配置 Nginx 反向代理

### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 许可证

MIT License
