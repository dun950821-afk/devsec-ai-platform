<template>
  <div class="rules-page">
    <div class="page-header">
      <h2 class="page-title">规则管理中心</h2>
    </div>

    <!-- 规则包信息 -->
    <div class="rulepack-info">
      <el-card>
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="info-item"><span class="label">规则包版本</span><span class="value">v2024.06.23</span></div>
          </el-col>
          <el-col :span="6">
            <div class="info-item"><span class="label">签名状态</span><el-tag type="success" size="small">已验证</el-tag></div>
          </el-col>
          <el-col :span="6">
            <div class="info-item"><span class="label">规则总数</span><span class="value">2,608 条</span></div>
          </el-col>
          <el-col :span="6" class="action-col">
            <el-button @click="downloadPack">下载</el-button>
            <el-button @click="rollbackPack">回滚</el-button>
            <el-button type="primary" @click="importPack">导入</el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 规则统计 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">1,520</span><span class="stat-label">SAST规则</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">680</span><span class="stat-label">SCA规则</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">256</span><span class="stat-label">Secrets规则</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">152</span><span class="stat-label">Baseline规则</span></div></el-col>
      </el-row>
    </div>

    <!-- 规则分类标签 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeType" @change="filterByType">
        <el-radio-button label="all">全部 (2,608)</el-radio-button>
        <el-radio-button label="sast">SAST (1,520)</el-radio-button>
        <el-radio-button label="sca">SCA (680)</el-radio-button>
        <el-radio-button label="secrets">Secrets (256)</el-radio-button>
        <el-radio-button label="baseline">Baseline (152)</el-radio-button>
        <el-radio-button label="custom">自定义 (0)</el-radio-button>
      </el-radio-group>
      <div class="filter-actions">
        <el-select v-model="filters.level" placeholder="严重等级" clearable style="width: 120px">
          <el-option label="Critical" value="critical" />
          <el-option label="High" value="high" />
          <el-option label="Medium" value="medium" />
          <el-option label="Low" value="low" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 100px">
          <el-option label="已启用" value="enabled" />
          <el-option label="已禁用" value="disabled" />
        </el-select>
        <el-input v-model="filters.keyword" placeholder="搜索规则名称/ID" clearable style="width: 200px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary">搜索</el-button>
      </div>
    </div>

    <!-- 规则列表 -->
    <div class="table-card">
      <el-table :data="ruleList" stripe style="width: 100%">
        <el-table-column prop="id" label="规则ID" width="140" />
        <el-table-column prop="name" label="规则名称" min-width="200" />
        <el-table-column prop="type" label="规则类型" width="100">
          <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="level" label="严重等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cwe" label="CWE映射" width="100" />
        <el-table-column prop="owasp" label="OWASP映射" width="100" />
        <el-table-column prop="fix" label="修复建议" min-width="200" />
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="toggleRule(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editRule(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Search } from '@element-plus/icons-vue';

const activeType = ref('all');
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(2608);

const filters = reactive({ level: '', status: '', keyword: '' });

const ruleList = ref([
  { id: 'SAST-JAVA-001', name: 'SQL注入', type: 'SAST', level: 'Critical', cwe: 'CWE-89', owasp: 'A1', fix: '使用预编译语句(PreparedStatement)', enabled: true },
  { id: 'SAST-JAVA-002', name: 'MyBatis ${} 拼接', type: 'SAST', level: 'High', cwe: 'CWE-89', owasp: 'A1', fix: '使用 #{} 参数绑定', enabled: true },
  { id: 'SAST-JAVA-003', name: 'SSRF 外部URL可控', type: 'SAST', level: 'High', cwe: 'CWE-918', owasp: 'A10', fix: '校验URL来源，禁止外部可控', enabled: true },
  { id: 'SAST-JAVA-004', name: 'XXE 外部实体未禁用', type: 'SAST', level: 'Critical', cwe: 'CWE-611', owasp: 'A4', fix: '禁用外部实体，升级XML解析库', enabled: true },
  { id: 'SAST-JAVA-005', name: 'Runtime.exec命令执行', type: 'SAST', level: 'High', cwe: 'CWE-78', owasp: 'A1', fix: '避免使用Runtime.exec，改用API调用', enabled: true },
  { id: 'SAST-JAVA-006', name: '路径穿越', type: 'SAST', level: 'High', cwe: 'CWE-22', owasp: 'A1', fix: '路径标准化，禁止用户输入拼接路径', enabled: true },
  { id: 'SAST-JAVA-007', name: '硬编码密码', type: 'Secrets', level: 'Critical', cwe: 'CWE-798', owasp: 'A2', fix: '使用配置中心或环境变量存储密码', enabled: true },
  { id: 'SAST-JAVA-008', name: '弱加密 MD5', type: 'SAST', level: 'Medium', cwe: 'CWE-327', owasp: 'A6', fix: '使用SHA-256或更强算法', enabled: true },
  { id: 'SAST-JAVA-009', name: 'CORS 全开放', type: 'SAST', level: 'Medium', cwe: 'CWE-942', owasp: 'A5', fix: '限制允许的来源域名', enabled: true },
  { id: 'SAST-JAVA-010', name: 'Fastjson autoType', type: 'SAST', level: 'Critical', cwe: 'CWE-502', owasp: 'A8', fix: '禁用autoType，升级到最新版本', enabled: true },
  { id: 'SAST-JAVA-011', name: '日志输出敏感信息', type: 'Secrets', level: 'Medium', cwe: 'CWE-532', owasp: 'A3', fix: '脱敏后再输出日志', enabled: true },
  { id: 'SAST-JAVA-012', name: 'XSS 反射型', type: 'SAST', level: 'Medium', cwe: 'CWE-79', owasp: 'A7', fix: '输入校验，输出编码', enabled: true }
]);

function getLevelType(level: string) {
  const map: Record<string, string> = { 'Critical': 'danger', 'High': 'warning', 'Medium': 'warning', 'Low': 'info' };
  return map[level] || 'info';
}

function filterByType() { console.log('筛选规则类型', activeType.value); }
function downloadPack() { console.log('下载规则包'); }
function rollbackPack() { console.log('回滚规则包'); }
function importPack() { console.log('导入规则包'); }
function toggleRule(row: any) { console.log('切换规则', row); }
function editRule(row: any) { console.log('编辑规则', row); }
</script>

<style scoped>
.rules-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }
.rulepack-info { margin-bottom: 20px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item .label { font-size: 12px; color: var(--color-on-surface-variant); }
.info-item .value { font-size: 14px; font-weight: 600; }
.action-col { display: flex; justify-content: flex-end; gap: 8px; }
.stats-row { margin-bottom: 20px; }
.stat-mini { background: white; padding: 16px 20px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-num { font-size: 28px; font-weight: 700; color: var(--color-on-surface); }
.stat-label { font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px; }
.filter-bar { background: white; padding: 16px 20px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); display: flex; justify-content: space-between; align-items: center; }
.filter-actions { display: flex; gap: 8px; }
.table-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
