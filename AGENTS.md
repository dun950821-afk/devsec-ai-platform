# DevSecAI 插件管理平台 - 项目文档

## 技术栈

### 前端
- **框架**: 纯 HTML5 + CSS3 + Vanilla JavaScript (ES6+)
- **语言**: 原生 JavaScript ES6+
- **样式**: 内联 CSS（深色侧边栏 + 浅色内容区）
- **HTTP客户端**: Fetch API + Bearer Token
- **架构**: 单页应用(SPA)，条件渲染切换页面

### 后端
- **框架**: Spring Boot 3.2
- **语言**: Java 17
- **数据库**: H2 (开发环境)
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **构建工具**: Maven

### IDEA插件
- **框架**: IntelliJ Platform Plugin SDK
- **语言**: Kotlin 1.9.22 (JVM Target 17)
- **构建工具**: Gradle 8.5 + IntelliJ Platform Gradle Plugin 1.17.2
- **目标平台**: IntelliJ IDEA 2024.1+
- **HTTP客户端**: java.net.HttpURLConnection + Gson
- **依赖**: OkHttp 4.12.0, Gson 2.10.1

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
│   │   ├── PluginInstanceController.java  # 插件实例接口（握手/心跳/策略）
│   │   ├── CapabilityPolicyController.java # 能力策略接口
│   │   ├── PluginTokenController.java  # Token管理接口
│   │   ├── FindingController.java  # Finding上送接口
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

### 插件实例管理（IDEA插件模块）
- `GET /api/plugin-instance/list` - 插件实例列表（分页+筛选）
- `GET /api/plugin-instance/{id}` - 插件实例详情
- `POST /api/plugin-instance/handshake` - 握手认证（无需JWT，需accessToken）
- `POST /api/plugin-instance/heartbeat` - 心跳上报（无需JWT）
- `GET /api/plugin-instance/policy` - 获取策略配置（无需JWT）
- `PUT /api/plugin-instance/{id}/status` - 更新插件实例状态
- `DELETE /api/plugin-instance/{id}` - 删除插件实例

### 能力策略管理
- `GET /api/capability-policy/list` - 策略列表
- `GET /api/capability-policy/{id}` - 策略详情
- `POST /api/capability-policy` - 创建策略
- `PUT /api/capability-policy/{id}` - 更新策略
- `DELETE /api/capability-policy/{id}` - 删除策略
- `PUT /api/capability-policy/{id}/status` - 更新策略状态

### Token管理
- `GET /api/plugin-token/list` - Token列表
- `GET /api/plugin-token/{id}` - Token详情
- `POST /api/plugin-token` - 生成Token
- `PUT /api/plugin-token/{id}/revoke` - 吊销Token

### Finding上送
- `GET /api/finding/list` - Finding列表（分页+筛选）
- `GET /api/finding/{id}` - Finding详情
- `POST /api/finding/upload` - Finding上送（支持单条/批量）
- `PUT /api/finding/{id}/status` - 更新Finding状态
- `GET /api/finding/severity-counts` - 严重等级统计

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

### IDEA插件 (/workspace/projects/devsec-ai-idea-plugin)
```
├── build.gradle.kts                    # Gradle 构建配置
├── settings.gradle.kts                 # Gradle 项目设置
├── gradle/wrapper/                     # Gradle Wrapper
├── src/main/kotlin/com/guoshun/devsecai/
│   ├── model/
│   │   └── Models.kt                   # 数据模型（Handshake/Policy/Finding等）
│   ├── config/
│   │   ├── DevSecAISettings.kt         # PersistentStateComponent 持久化配置
│   │   └── DevSecAISettingsConfigurable.kt  # Settings UI面板
│   ├── service/
│   │   ├── DevSecAIClient.kt           # HTTP客户端（握手/心跳/策略/Finding上送）
│   │   ├── HeartbeatService.kt         # 定时心跳服务（5分钟间隔）
│   │   ├── PolicyService.kt            # 策略拉取+缓存
│   │   ├── FindingCollector.kt         # Finding收集器（线程安全+批量上送）
│   │   └── ProjectStartupListener.kt   # 项目启动监听（触发握手+心跳+策略）
│   ├── inspection/
│   │   ├── SecurityInspectionTool.kt   # SAST检测（SQL注入/XSS/命令注入/弱加密等）
│   │   └── SecretsInspectionTool.kt    # Secrets检测（AWS/GitHub/JWT/密码/私钥等）
│   ├── ui/
│   │   ├── DevSecAIToolWindowFactory.kt # ToolWindow工厂
│   │   └── DevSecAIToolWindowPanel.kt  # 检测结果面板（表格+严重等级筛选+颜色标记）
│   └── action/
│       ├── SecurityCheckinHandlerFactory.kt  # Git Commit拦截（高危Finding阻断提交）
│       ├── ManualScanAction.kt         # 手动触发全项目安全扫描
│       └── UploadFindingsAction.kt     # 手动上送检测结果
├── src/main/resources/
│   └── META-INF/plugin.xml             # IntelliJ 插件描述符
└── .coze
```

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

### IDEA插件
```bash
cd /workspace/projects/devsec-ai-idea-plugin
# 构建（需要网络下载 IntelliJ Platform SDK）
./gradlew buildPlugin
# 开发模式运行
./gradlew runIde
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
| 工作台 | ✅ | 统计数据仪表盘 + 最近检测活动 + 风险分布 |
| 插件实例管理 | ✅ | IDEA插件握手认证、心跳上报、策略下发、6值状态管理 |
| 能力配置 | ✅ | 12项能力开关策略管理（检测/AI/提交/数据4组）、项目绑定 |
| Token管理 | ✅ | 访问令牌生成、吊销、绑定插件、脱敏显示 |
| Finding上送 | ✅ | SCA/SAST检测结果上送（单条/批量）、严重等级统计 |
| 插件管理 | ✅ | CRUD、启用/禁用、心跳检测 |
| 项目管理 | ✅ | CRUD、项目配置 |
| 检测策略 | ✅ | CRUD、策略配置 |
| 规则管理 | ✅ | CRUD、严重性分级 |
| 检测结果 | ✅ | 列表展示、状态筛选 |
| AI Skill | ✅ | CRUD、Skill配置 |
| 审计日志 | ✅ | 操作日志记录 |
| 系统设置 | ✅ | 基本信息展示 |
| IDEA插件-握手认证 | ✅ | AccessToken握手、项目绑定、策略下发 |
| IDEA插件-心跳上报 | ✅ | 5分钟定时心跳、离线检测 |
| IDEA插件-SAST检测 | ✅ | SQL注入/XSS/命令注入/路径遍历/反序列化/弱加密/不安全Socket |
| IDEA插件-Secrets检测 | ✅ | AWS/GitHub/JWT/密码/私钥/数据库密码/API Key正则检测 |
| IDEA插件-策略管理 | ✅ | 策略拉取+5分钟缓存、能力开关判断 |
| IDEA插件-Finding收集 | ✅ | 线程安全队列、30秒批量上送、按module分组 |
| IDEA插件-Git Commit拦截 | ✅ | 严重/高危Finding阻断提交、用户确认 |
| IDEA插件-ToolWindow | ✅ | 检测结果表格、严重等级筛选+颜色标记 |
| IDEA插件-Settings | ✅ | 服务器地址/Token/功能开关配置面板 |
