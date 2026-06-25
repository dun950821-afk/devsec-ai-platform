# DevSecAI 插件管理平台 - 开发完善计划

## 一、现状总览

### 后端 API（已完整）
| 模块 | 控制器 | 核心接口 | 状态 |
|------|--------|---------|------|
| 认证 | AuthController | login/logout/register/current | ✅ 完整 |
| 仪表盘 | MainController | dashboard | ✅ 完整 |
| 用户 | MainController | user/list, user/{id}, user CRUD, user/{id}/status | ✅ 完整 |
| 插件 | PluginController | CRUD + status + config + heartbeat + enabled | ✅ 完整 |
| 项目 | ProjectController | CRUD + bindPolicy + toggleScan | ✅ 完整 |
| 策略 | PolicyController | CRUD + setDefault | ✅ 完整 |
| 规则 | RuleController | CRUD + status | ✅ 完整 |
| 结果 | ResultController | list + detail + finishScan + status | ✅ 完整 |
| Skill | SkillController | CRUD + status | ✅ 完整 |
| 审计 | AuditController | list(支持日期+操作类型筛选) | ✅ 完整 |

### 后端实体字段（关键差异项）
| 实体 | 已有但前端未用的字段 |
|------|---------------------|
| User | email, phone, role(admin/user), status |
| Plugin | pluginKey, category(code_scanner/dependency_check/secret_detection), config(JSON), endpoint, status(0-4状态) |
| Project | code, userId, scanEnabled, policyId, status(0-2) |
| ScanPolicy | ruleIds(逗号分隔), pluginIds(逗号分隔), scanType(0实时/1定时/2手动), cronExpression, isDefault |
| ScanRule | severity(critical/high/medium/low/info), category(code_quality/security/best_practice), pattern, solution, pluginId |
| ScanResult | projectId, policyId, branch, commitId, status(0扫描中/1完成/2失败), criticalCount/highCount/mediumCount/lowCount/infoCount, summary(JSON) |
| Skill | skillKey, type(analyzer/fixer/reviewer), provider(openai/claude/local), model, config(JSON) |
| AuditLog | module, operation, description, method, url, param, ip, status(0失败/1成功), errorMsg |

### 前端现状
- 纯 HTML/CSS/JS 单文件应用（index.html, ~1183行）
- 仅登录页功能完整
- 各模块页面仅基础列表+模态框CRUD
- 大量后端字段未展示、未交互

---

## 二、分阶段完善计划

### 第一阶段：核心功能补全（P0）

#### 1.1 首页总览仪表盘重构
**目标**：从简单的数字卡片升级为设计文档要求的安全总览

**后端API**：`GET /api/dashboard`（已有，返回 totalProjects/activeProjects/totalPlugins/activePlugins/todayScans/totalIssues/criticalIssues/highIssues/mediumIssues/lowIssues）

**前端改造**：
- [ ] 4个统计卡片：活跃项目数、今日扫描数、活跃插件数、总问题数
- [ ] 风险等级分布：Critical/High/Medium/Low 4色环形统计（纯CSS实现）
- [ ] 最近扫描记录表格：调用 `GET /api/result/list`，展示项目名/扫描状态/问题数/时间
- [ ] 插件状态概览：调用 `GET /api/plugin/list`，展示在线/离线数量

**涉及文件**：`index.html` 中 `#page-dashboard` 区域

---

#### 1.2 用户管理页面
**目标**：完整的用户CRUD管理，设计文档要求的三种角色

**后端API**：
- `GET /api/user/list?keyword=&current=1&size=10`
- `POST /api/user`（创建用户）
- `PUT /api/user/{id}`（更新用户）
- `PUT /api/user/{id}/status`（启用/禁用）
- `GET /api/user/{id}`（详情）

**前端改造**：
- [ ] 侧边栏新增「用户管理」菜单项（在系统分组下）
- [ ] 用户列表表格：用户名/姓名/邮箱/手机/角色/状态/操作
- [ ] 角色筛选：admin管理员/user普通用户
- [ ] 新增/编辑用户模态框：username/realName/email/phone/role/password
- [ ] 启用/禁用状态切换

**涉及文件**：`index.html` 新增 `#page-user` 区域

---

#### 1.3 检测结果中心重构
**目标**：从简单列表升级为Finding详情中心

**后端API**：
- `GET /api/result/list?projectId=&status=&current=1&size=10`
- `GET /api/result/{id}`（详情，含 summary JSON）
- `PUT /api/result/{id}/status`（状态更新）

**前端改造**：
- [ ] 结果列表表格：项目/策略/分支/状态(扫描中/完成/失败)/各级别问题数/时间
- [ ] 状态筛选：0扫描中/1完成/2失败
- [ ] 项目筛选下拉框（调用 `GET /api/project/list`）
- [ ] 点击行展开详情面板：显示 criticalCount/highCount/mediumCount/lowCount/infoCount + summary JSON
- [ ] 状态标记操作：完成/失败

**涉及文件**：`index.html` 中 `#page-result` 区域

---

#### 1.4 策略管理重构
**目标**：展示设计文档要求的扫描类型、规则绑定、插件绑定等

**后端API**：
- `GET /api/policy/list?keyword=&current=1&size=10`
- `GET /api/policy/{id}`
- `POST /api/policy`
- `PUT /api/policy/{id}`
- `DELETE /api/policy/{id}`
- `PUT /api/policy/{id}/default`（设为默认策略）
- `GET /api/rule/list`（获取规则列表供绑定选择）
- `GET /api/plugin/list`（获取插件列表供绑定选择）

**前端改造**：
- [ ] 策略列表：名称/扫描类型(实时/定时/手动)/是否默认/规则数/插件数/操作
- [ ] 新增/编辑模态框：
  - 基础字段：name, description
  - 扫描类型：select(实时扫描/定时扫描/手动触发)
  - Cron表达式：scanType=1时显示
  - 规则绑定：多选checkbox，从规则列表中选取
  - 插件绑定：多选checkbox，从插件列表中选取
- [ ] 设为默认策略按钮
- [ ] isDefault 标签展示

**涉及文件**：`index.html` 中 `#page-policy` 区域

---

### 第二阶段：重要功能补全（P1）

#### 2.1 插件管理重构
**目标**：展示设计文档要求的插件类别、配置、心跳状态

**后端API**：
- `GET /api/plugin/list?keyword=&category=&status=&current=1&size=10`
- `POST /api/plugin`
- `PUT /api/plugin/{id}`
- `DELETE /api/plugin/{id}`
- `PUT /api/plugin/{id}/status`
- `PUT /api/plugin/{id}/config`（更新配置JSON）
- `POST /api/plugin/heartbeat`

**前端改造**：
- [ ] 列表增加字段：pluginKey, category标签(代码扫描/依赖检查/密钥检测), endpoint
- [ ] 状态从2值升级为4值(0未启用/1启用/2运行中/3异常)，4色标签
- [ ] 类别筛选下拉：code_scanner/dependency_check/secret_detection
- [ ] 新增/编辑模态框增加：pluginKey, category, endpoint, config(JSON编辑器textarea)
- [ ] 心跳状态列：最后心跳时间，超时标红
- [ ] 配置按钮 → 弹出JSON配置编辑模态框

**涉及文件**：`index.html` 中 `#page-plugin` 区域

---

#### 2.2 项目管理重构
**目标**：展示设计文档要求的安全评分、策略绑定、扫描开关

**后端API**：
- `GET /api/project/list?keyword=&status=&current=1&size=10`
- `POST /api/project`
- `PUT /api/project/{id}`
- `DELETE /api/project/{id}`
- `PUT /api/project/{id}/policy?policyId=`（绑定策略）
- `PUT /api/project/{id}/scan?enabled=`（扫描开关）
- `GET /api/policy/list`（获取策略列表供选择）

**前端改造**：
- [ ] 列表增加字段：code, scanEnabled开关, policyId绑定的策略名
- [ ] 状态从2值升级为3值(0正常/1暂停/2异常)
- [ ] 新增/编辑模态框增加：code, scanEnabled, policyId(下拉选择策略)
- [ ] 策略绑定：下拉选择策略 → 调用 `PUT /api/project/{id}/policy`
- [ ] 扫描开关：toggle switch → 调用 `PUT /api/project/{id}/scan`

**涉及文件**：`index.html` 中 `#page-project` 区域

---

#### 2.3 规则管理重构
**目标**：展示CWE映射、修复建议、检测模式

**后端API**：
- `GET /api/rule/list?keyword=&severity=&category=&current=1&size=10`
- `GET /api/rule/{id}`
- `POST /api/rule`
- `PUT /api/rule/{id}`
- `DELETE /api/rule/{id}`
- `PUT /api/rule/{id}/status`

**前端改造**：
- [ ] 列表增加字段：category标签(代码质量/安全/最佳实践), pattern, solution
- [ ] severity从3级升级为5级(critical/high/medium/low/info)
- [ ] category筛选下拉：code_quality/security/best_practice
- [ ] 新增/编辑模态框增加：category, pattern, solution, pluginId(下拉选择插件)
- [ ] 点击行展开修复建议(solution)

**涉及文件**：`index.html` 中 `#page-rule` 区域

---

#### 2.4 AI Skill 重构
**目标**：展示设计文档要求的3种Skill类型、provider、模型配置

**后端API**：
- `GET /api/skill/list?keyword=&type=&current=1&size=10`
- `POST /api/skill`
- `PUT /api/skill/{id}`
- `DELETE /api/skill/{id}`
- `PUT /api/skill/{id}/status`

**前端改造**：
- [ ] 列表增加字段：skillKey, type标签(分析器/修复器/审查器), provider, model
- [ ] type筛选下拉：analyzer/fixer/reviewer
- [ ] 新增/编辑模态框增加：skillKey, type, provider(openai/claude/local), model, config(JSON)
- [ ] 状态切换：启用/禁用

**涉及文件**：`index.html` 中 `#page-skill` 区域

---

### 第三阶段：增强体验（P2）

#### 3.1 审计日志增强
**目标**：丰富筛选、详情展示

**后端API**：`GET /api/audit/list?keyword=&operation=&startDate=&endDate=&current=1&size=10`

**前端改造**：
- [ ] operation筛选下拉：LOGIN/LOGOUT/CREATE/UPDATE/DELETE/SCAN
- [ ] 日期范围筛选：startDate/endDate
- [ ] 列表增加字段：module, description, method+url, ip, status(成功/失败标签)
- [ ] 点击行展开详情：param请求参数, errorMsg错误信息

**涉及文件**：`index.html` 中 `#page-audit` 区域

---

#### 3.2 系统设置增强
**目标**：可编辑的系统配置

**前端改造**：
- [ ] 基本信息区：系统名称/版本/邮箱（可编辑保存）
- [ ] AI模型配置区：provider/model/apiKey 配置（对应Skill的provider配置）
- [ ] 通知配置区：邮件/钉钉/企业微信 webhook
- [ ] 安全策略区：会话超时/密码策略

**注意**：后端暂无设置相关API，此阶段可先用 localStorage 存储

**涉及文件**：`index.html` 中 `#page-settings` 区域

---

## 三、实施顺序与依赖

```
阶段1（核心）→ 阶段2（重要）→ 阶段3（增强）

1.1 首页仪表盘 ← 无依赖，最先做
1.2 用户管理    ← 无依赖
1.3 检测结果    ← 依赖项目列表API
1.4 策略管理    ← 依赖规则+插件列表API

2.1 插件管理    ← 无依赖
2.2 项目管理    ← 依赖策略列表API
2.3 规则管理    ← 依赖插件列表API
2.4 AI Skill    ← 无依赖

3.1 审计日志    ← 无依赖
3.2 系统设置    ← 最后做
```

## 四、每个任务的前端改造模式

由于是纯 HTML 单文件应用，每个模块改造遵循统一模式：

1. **列表表格**：增加列 → 调整 `renderXxxTable()` 函数
2. **筛选栏**：增加筛选条件 → 调整 `loadXxxs()` 函数参数
3. **模态框**：增加表单字段 → 调整 `showXxxModal()` / `saveXxx()` 函数
4. **API调用**：对齐后端实际参数名（keyword代替name、分页参数current/size）

## 五、验收标准

- 每个模块完成后可通过浏览器完整操作（增删改查）
- 列表数据来自后端API真实返回
- 状态标签颜色与设计文档一致
- 模态框表单字段与后端实体对齐
