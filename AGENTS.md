# DevSecAI 插件管理平台 - 项目文档

## 技术栈

### 前端
- **框架**: 纯 HTML5 + CSS3 + Vanilla JavaScript (已修改)
- **语言**: 原生 JavaScript ES6+
- **样式**: 内联 CSS
- **HTTP客户端**: Fetch API

### 后端
- **框架**: Spring Boot 3.2
- **语言**: Java 17
- **数据库**: H2 (开发环境)
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **构建工具**: Maven

## 目录结构

### 前端 (/workspace/projects)
```
/
├── index.html            # 主入口页面（单页应用）
├── server.cjs            # Node.js 静态服务器（含API代理）
├── .coze                 # Coze 部署配置
└── public/               # 静态资源目录
```

### 后端 (/workspace/projects/devsec-ai-server)
```
├── src/main/java/com/guoshun/devsecai/
│   ├── DevSecAIApplication.java     # 应用入口
│   ├── config/                      # 配置类
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   └── DatabaseInitRunner.java  # 数据库初始化
│   ├── controller/                  # 控制器层
│   │   ├── AuthController.java     # 认证接口
│   │   ├── MainController.java     # 主控制器
│   │   ├── PluginController.java   # 插件接口
│   │   ├── ProjectController.java  # 项目接口
│   │   ├── PolicyController.java    # 策略接口
│   │   ├── RuleController.java      # 规则接口
│   │   ├── ResultController.java    # 结果接口
│   │   ├── SkillController.java     # Skill接口
│   │   └── AuditController.java     # 审计日志接口
│   ├── service/                     # 业务逻辑层
│   ├── mapper/                      # 数据访问层
│   ├── entity/                      # 实体类
│   ├── dto/                         # 数据传输对象
│   ├── security/                    # 安全相关
│   │   ├── JwtTokenUtil.java       # JWT工具类
│   │   ├── JwtAuthenticationFilter.java  # JWT过滤器
│   │   └── JwtAuthenticationEntryPoint.java
│   └── common/                      # 公共类
├── src/main/resources/
│   ├── application.yml              # 主配置文件
│   ├── application-dev.yml          # 开发环境配置
│   └── mapper/                      # MyBatis XML
├── pom.xml
└── .coze
```

## API 接口

### 认证模块
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户退出

### 仪表盘
- `GET /api/dashboard` - 获取仪表盘数据

### 用户管理
- `GET /api/user/list` - 用户列表
- `GET /api/user/{id}` - 获取用户详情
- `POST /api/user` - 创建用户
- `PUT /api/user/{id}` - 更新用户
- `PUT /api/user/{id}/status` - 更新用户状态

### 插件管理
- `GET /api/plugin/list` - 插件列表
- `GET /api/plugin/{id}` - 插件详情
- `GET /api/plugin/enabled` - 已启用插件
- `POST /api/plugin` - 创建插件
- `PUT /api/plugin/{id}` - 更新插件
- `DELETE /api/plugin/{id}` - 删除插件
- `PUT /api/plugin/{id}/status` - 更新插件状态
- `POST /api/plugin/heartbeat` - 插件心跳

### 项目管理
- `GET /api/project/list` - 项目列表
- `GET /api/project/{id}` - 项目详情
- `POST /api/project` - 创建项目
- `PUT /api/project/{id}` - 更新项目
- `DELETE /api/project/{id}` - 删除项目
- `PUT /api/project/{id}/status` - 更新项目状态

### 策略管理
- `GET /api/policy/list` - 策略列表
- `GET /api/policy/{id}` - 策略详情
- `POST /api/policy` - 创建策略
- `PUT /api/policy/{id}` - 更新策略
- `DELETE /api/policy/{id}` - 删除策略
- `PUT /api/policy/{id}/status` - 更新策略状态

### 规则管理
- `GET /api/rule/list` - 规则列表
- `GET /api/rule/{id}` - 规则详情
- `POST /api/rule` - 创建规则
- `PUT /api/rule/{id}` - 更新规则
- `DELETE /api/rule/{id}` - 删除规则
- `PUT /api/rule/{id}/status` - 更新规则状态

### 检测结果
- `GET /api/result/list` - 结果列表
- `GET /api/result/{id}` - 结果详情
- `PUT /api/result/{id}/status` - 更新结果状态

### Skill管理
- `GET /api/skill/list` - Skill列表
- `GET /api/skill/{id}` - Skill详情
- `POST /api/skill` - 创建Skill
- `PUT /api/skill/{id}` - 更新Skill
- `DELETE /api/skill/{id}` - 删除Skill
- `PUT /api/skill/{id}/status` - 更新Skill状态

### 审计日志
- `GET /api/audit/list` - 审计日志列表

## 启动命令

### 前端（静态服务器 + API代理）
```bash
cd /workspace/projects
node server.cjs
```

### 后端
```bash
cd /workspace/projects/devsec-ai-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 默认账户
- 用户名: admin
- 密码: admin123

### 端口配置
- 前端: 5000
- 后端: 8080

## 功能模块

### 已实现的功能

| 模块 | 状态 | 说明 |
|-----|------|------|
| 用户登录 | ✅ | JWT认证，支持登录/登出 |
| 工作台 | ✅ | 统计数据仪表盘 + 最近检测结果 |
| 插件管理 | ✅ | CRUD、启用/禁用、心跳检测 |
| 项目管理 | ✅ | CRUD、项目配置 |
| 检测策略 | ✅ | CRUD、策略配置 |
| 规则管理 | ✅ | CRUD、严重性分级 |
| 检测结果 | ✅ | 列表展示、状态筛选 |
| AI Skill | ✅ | CRUD、Skill配置 |
| 审计日志 | ✅ | 操作日志记录 |
| 系统设置 | ✅ | 基本信息展示 |
