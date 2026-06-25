# IDEA 检查插件模块开发计划

## 概述

根据设计文档，在现有 DevSecAI 后台管理平台基础上，开发 IDEA 插件管理模块的后台功能，涵盖插件实例注册、握手认证、心跳上报、能力配置、策略下发、检测结果接收等完整流程。平台为 Web 端，纯前端 + Spring Boot 后端架构，不引入额外集成。

## 技术方案

| 维度 | 选择 | 理由 |
|------|------|------|
| 前端框架 | 纯 HTML/CSS/JS | 与现有项目保持一致 |
| 后端框架 | Spring Boot 3.2 + MyBatis Plus | 与现有后端一致 |
| 数据库 | H2 (dev) | 与现有一致 |
| 认证方式 | Token + JWT | 设计文档要求插件用 Token 认证 |
| 前后端通信 | REST API + 代理 | 与现有架构一致 |

## 功能模块

### 模块 A：插件实例管理（后台页面）

**职责**：管理所有 IDEA 插件实例的注册、状态、心跳、绑定项目。

**数据结构 — plugin_instance**：
```json
{
  "id": 1,
  "pluginId": "IDEA-PLG-000128",
  "pluginVersion": "0.9.6",
  "ideName": "IntelliJ IDEA",
  "ideVersion": "2025.2",
  "developer": "张三",
  "machineId": "hash-machine-id",
  "projectId": 1,
  "projectName": "支付核心系统",
  "policyId": 1,
  "policyName": "银行Java安全策略",
  "status": "ONLINE",
  "lastHeartbeat": "2026-06-25 10:30:00",
  "accessToken": "plt_xxx",
  "enabledModules": "SCA,SAST,SECRETS,BASELINE,AI",
  "createTime": "2026-06-20 09:00:00",
  "updateTime": "2026-06-25 10:30:00"
}
```

**插件状态**：ONLINE / OFFLINE / ERROR / UPGRADE_REQUIRED / DISABLED / UNAUTHENTICATED

**页面字段**：插件ID / 开发者 / 所属项目 / IDE类型 / 插件版本 / 当前策略 / 最近心跳 / 状态 / 操作

### 模块 B：插件握手认证（后台API）

**职责**：插件启动时握手，后台校验 Token 后返回用户、项目、策略、规则包版本、启用模块和 AI 权限。

**握手请求** `POST /api/plugin/handshake`：
```json
{
  "pluginId": "IDEA-PLG-000128",
  "pluginVersion": "0.9.6",
  "ideName": "IntelliJ IDEA",
  "ideVersion": "2025.2",
  "projectName": "payment-service",
  "projectGitUrl": "https://git.example.com/bank/payment-service.git",
  "machineId": "hash-machine-id",
  "accessToken": "plt_xxx"
}
```

**握手响应**：
```json
{
  "success": true,
  "data": {
    "projectId": "payment-core-system",
    "projectName": "支付核心系统",
    "userId": "zhangsan",
    "policyId": "bank-java-secure-dev",
    "policyVersion": "1.0.3",
    "rulePackVersion": "2026.06.23",
    "enabledModules": ["SCA", "SAST", "SECRETS", "BASELINE", "AI"]
  }
}
```

### 模块 C：插件心跳上报（后台API）

**职责**：插件定时上报心跳，后台更新在线状态。

**心跳请求** `POST /api/plugin/heartbeat`：
```json
{
  "pluginId": "IDEA-PLG-000128",
  "status": "ONLINE",
  "activeModules": ["SCA", "SAST"],
  "scanCount": 12,
  "findingCount": 3
}
```

### 模块 D：插件能力配置（后台页面+API）

**职责**：后台统一控制插件的 SCA/SAST/Secrets/Baseline/AI 等能力开关，按项目/组织/用户绑定策略。

**能力开关项**：
| 能力 | 说明 |
|------|------|
| SCA | 开源组件检测 |
| SAST | 代码安全检测 |
| Secrets | 敏感信息检测 |
| Baseline | 企业基线检测 |
| AI 漏洞解释 | 调用大模型解释漏洞 |
| AI 修复建议 | 调用大模型生成修复建议 |
| AI Patch 生成 | 生成代码修改片段 |
| Git Commit 检查 | 提交前自动扫描 |
| 高危阻断提交 | Critical/High 风险禁止提交 |
| 结果自动上送 | Finding 元数据上送后台 |
| 代码片段上传 | 控制是否允许上传代码片段 |
| 脱敏策略 | 控制 Secrets 脱敏方式 |

**数据结构 — plugin_capability_policy**：
```json
{
  "id": 1,
  "policyName": "银行Java安全检测策略",
  "policyVersion": "1.0.3",
  "scaEnabled": true,
  "sastEnabled": true,
  "secretsEnabled": true,
  "baselineEnabled": true,
  "aiEnabled": true,
  "aiVulnExplain": true,
  "aiFixSuggestion": true,
  "aiPatchGenerate": true,
  "gitCommitCheck": true,
  "blockCritical": true,
  "blockHigh": true,
  "blockMedium": false,
  "blockLow": false,
  "autoUpload": true,
  "allowCodeSnippet": true,
  "maskSecrets": true,
  "maxContextLines": 80,
  "projectId": 1,
  "status": 1,
  "createTime": "2026-06-20 09:00:00"
}
```

### 模块 E：检测结果上送（后台API）

**职责**：插件扫描完成后上送 Finding 数据，后台存储并汇总统计。

**SCA Finding** `POST /api/finding/upload`：
```json
{
  "pluginId": "IDEA-PLG-000128",
  "module": "SCA",
  "ruleId": "SCA-MAVEN-LOG4J-CVE-2021-44228",
  "severity": "CRITICAL",
  "componentName": "org.apache.logging.log4j:log4j-core",
  "currentVersion": "2.14.1",
  "fixedVersion": "2.17.1",
  "vulnerabilityId": "CVE-2021-44228",
  "filePath": "pom.xml",
  "status": "OPEN"
}
```

**SAST Finding**：
```json
{
  "pluginId": "IDEA-PLG-000128",
  "module": "SAST",
  "ruleId": "JAVA-SQL-INJECTION-001",
  "title": "疑似SQL注入风险",
  "severity": "HIGH",
  "confidence": "MEDIUM",
  "filePath": "src/main/java/com/example/UserMapper.java",
  "startLine": 42,
  "endLine": 44,
  "description": "检测到MyBatis使用${}拼接外部参数",
  "recommendation": "建议使用#{}参数绑定",
  "cwe": "CWE-89",
  "owasp": "A03:2021-Injection",
  "status": "OPEN"
}
```

### 模块 F：策略下发（后台API）

**职责**：插件拉取能力策略和检测策略，后台返回完整的策略配置。

**策略拉取** `GET /api/plugin/policy?pluginId=IDEA-PLG-000128`：
```json
{
  "policyId": "bank-java-secure-dev",
  "policyName": "银行Java安全检测策略",
  "policyVersion": "1.0.3",
  "enabledModules": { "sca": true, "sast": true, "secrets": true, "baseline": true, "ai": true },
  "scanTriggers": { "manualScan": true, "onSave": true, "onCommit": true },
  "blockingRules": { "critical": true, "high": true, "medium": false, "low": false },
  "aiPolicy": { "allowCodeSnippetUpload": true, "maskSecrets": true, "maxContextLines": 80 }
}
```

### 模块 G：插件访问 Token 管理（后台页面）

**职责**：管理插件的访问 Token，用于握手认证。生成、吊销、查看绑定插件。

**数据结构 — plugin_token**：
```json
{
  "id": 1,
  "token": "plt_xxxxxxxxxxxx",
  "name": "支付团队Token",
  "userId": 1,
  "bindPluginId": "IDEA-PLG-000128",
  "status": "ACTIVE",
  "expireTime": "2027-06-25 00:00:00",
  "createTime": "2026-06-25 10:00:00"
}
```

## 是否有原型设计

是（设计引导工具已开启）

## 实施步骤

1. **后端：新增实体和数据库表** — 创建 PluginInstance、PluginCapabilityPolicy、PluginToken 实体类及 MyBatis Mapper，使用 DatabaseInitRunner 初始化表结构和示例数据。涉及文件：entity/PluginInstance.java、entity/PluginCapabilityPolicy.java、entity/PluginToken.java、config/DatabaseInitRunner.java

2. **后端：实现插件握手、心跳、策略下发、Finding上送 API** — 新增 PluginHandshakeController（握手+心跳+策略拉取）和 FindingController（检测结果上送），包含 Token 校验逻辑。涉及文件：controller/PluginHandshakeController.java、controller/FindingController.java、service/PluginHandshakeService.java

3. **后端：完善插件实例管理 CRUD 和能力配置 CRUD** — 扩展 PluginController 增加实例列表/详情/禁用/启用/策略下发，新增 CapabilityPolicyController 管理能力配置策略。涉及文件：controller/PluginController.java、controller/CapabilityPolicyController.java

4. **后端：插件 Token 管理 API** — 新增 PluginTokenController，实现 Token 生成、列表、吊销。涉及文件：controller/PluginTokenController.java

5. **前端：插件管理页面重构** — 重构插件管理页面展示实例信息（插件ID/开发者/项目/IDE版本/状态6值/心跳），增加握手/心跳日志查看、策略下发操作、禁用操作。涉及文件：index.html

6. **前端：能力配置页面** — 新增能力配置页面，展示12项开关，支持按项目绑定策略，版本管理。涉及文件：index.html

7. **前端：Token 管理和检测结果页面增强** — 新增 Token 管理页面（生成/吊销/查看绑定），增强检测结果页面增加 SCA/SAST Finding 详情展示（组件版本/漏洞编号/修复版本/代码行/CWE等）。涉及文件：index.html

8. **联调测试** — 用 curl 逐一测试握手、心跳、策略拉取、Finding 上送、Token 管理 API，验证前端页面数据展示。涉及文件：无

## 页面规格

##### @nav(web-topbar)
> type: topbar
> platform: web

- @page(/) 首页
- @page(/plugin) 插件管理
- @page(/capability) 能力配置
- @page(/token) Token管理

##### @page(/) 首页总览

**核心职责**：展示全局安全指标概览和最近检测动态
**访问路径**：导航直达
**布局**：顶部统计卡片(6项) → 风险等级分布 → 最近扫描列表
**列表项字段**：项目名称 / 检测类型 / 严重等级 / 状态 / 时间

##### @page(/plugin) 插件管理

**核心职责**：管理所有 IDEA 插件实例的注册、状态和策略
**访问路径**：导航直达
**布局**：顶部操作栏(搜索+类型筛选+状态筛选) → 插件实例表格
**列表项字段**：插件ID / 开发者 / 所属项目 / IDE类型+版本 / 插件版本 / 当前策略 / 最近心跳 / 状态 / 操作

**状态**：
- 空态：暂无插件实例
- 加载态：加载中...

**交互说明**

| 元素 | 动作 | 响应 | 传参 | 备注 |
|------|------|------|------|------|
| 搜索框 | 输入 | 按插件ID/开发者搜索 | keyword | 防抖300ms |
| 状态筛选 | 选择 | 按状态过滤 | status | 6种状态 |
| 详情按钮 | 点击 | 弹出详情面板 | pluginId | 展示握手记录、心跳历史 |
| 下发策略 | 点击 | 弹出策略选择框 | pluginId | 选择能力策略后下发 |
| 禁用按钮 | 点击 | 确认后禁用插件 | pluginId | 禁用后插件无法调用API |
| 查看日志 | 点击 | 弹出心跳/状态日志 | pluginId | 时间线展示 |

**弹窗 plugin-detail**：
- 标题：插件详情
- 内容：插件完整信息（ID/版本/IDE/机器码/项目/策略/启用模块/心跳历史）
- 操作：关闭

**弹窗 policy-assign**：
- 标题：下发策略
- 内容：策略选择下拉框
- 操作：确认下发、取消

##### @page(/capability) 能力配置

**核心职责**：管理插件能力开关策略，绑定到项目
**访问路径**：导航直达
**布局**：顶部操作栏(新建策略) → 策略列表表格
**列表项字段**：策略名称 / 版本 / 绑定项目 / 启用模块 / 状态 / 操作

**状态**：
- 空态：暂无策略，请点击新建

**交互说明**

| 元素 | 动作 | 响应 | 传参 | 备注 |
|------|------|------|------|------|
| 新建策略 | 点击 | 弹出策略编辑表单 | — | 12项开关+基本信息 |
| 编辑按钮 | 点击 | 弹出策略编辑表单 | policyId | 预填现有值 |
| 开关项 | 切换 | 即时标记未保存 | — | 离开前提醒保存 |
| 绑定项目 | 点击 | 弹出项目选择 | policyId | 多选项目 |
| 删除按钮 | 点击 | 确认后删除 | policyId | 已绑定项目时提示 |

**弹窗 capability-form**：
- 标题：新建/编辑能力策略
- 内容：策略名称 + 12项开关(分为4组：检测能力/AI能力/提交控制/数据策略)
- 操作：保存、取消

##### @page(/token) Token管理

**核心职责**：管理插件访问 Token 的生成、吊销和绑定查看
**访问路径**：导航直达
**布局**：顶部操作栏(生成Token) → Token列表表格
**列表项字段**：Token(脱敏) / 名称 / 绑定插件 / 状态 / 过期时间 / 创建时间 / 操作

**交互说明**

| 元素 | 动作 | 响应 | 传参 | 备注 |
|------|------|------|------|------|
| 生成Token | 点击 | 弹出生成表单 | — | 填写名称和过期时间 |
| 复制Token | 点击 | 复制到剪贴板 | token | 仅创建时可见完整Token |
| 吊销按钮 | 点击 | 确认后吊销 | tokenId | 吊销后插件握手失败 |

**弹窗 token-generate**：
- 标题：生成访问Token
- 内容：Token名称 + 过期时间选择
- 操作：生成、取消
