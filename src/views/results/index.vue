<template>
  <div class="results-page">
    <div class="page-header">
      <h2 class="page-title">检测结果中心</h2>
      <el-button @click="exportReport">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="4"><div class="stat-mini"><span class="stat-num">1,286</span><span class="stat-label">待修复</span></div></el-col>
        <el-col :span="4"><div class="stat-mini"><span class="stat-num">234</span><span class="stat-label">修复中</span></div></el-col>
        <el-col :span="4"><div class="stat-mini"><span class="stat-num">3,568</span><span class="stat-label">已修复</span></div></el-col>
        <el-col :span="4"><div class="stat-mini"><span class="stat-num">456</span><span class="stat-label">已忽略</span></div></el-col>
        <el-col :span="4"><div class="stat-mini error"><span class="stat-num">89</span><span class="stat-label">高危风险</span></div></el-col>
        <el-col :span="4"><div class="stat-mini"><span class="stat-num">94.2%</span><span class="stat-label">修复率</span></div></el-col>
      </el-row>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-form :inline="true">
        <el-form-item label="所属项目">
          <el-select v-model="filters.project" placeholder="请选择" clearable style="width: 160px">
            <el-option label="全部项目" value="" />
            <el-option label="金融支付核心系统" value="payment" />
            <el-option label="用户中心服务" value="user" />
            <el-option label="交易网关系统" value="gateway" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险类型">
          <el-select v-model="filters.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="SAST" value="sast" />
            <el-option label="SCA" value="sca" />
            <el-option label="Secrets" value="secrets" />
            <el-option label="Baseline" value="baseline" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重等级">
          <el-select v-model="filters.level" placeholder="请选择" clearable style="width: 100px">
            <el-option label="全部" value="" />
            <el-option label="Critical" value="critical" />
            <el-option label="High" value="high" />
            <el-option label="Medium" value="medium" />
            <el-option label="Low" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="修复状态">
          <el-select v-model="filters.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待修复" value="open" />
            <el-option label="修复中" value="fixing" />
            <el-option label="已修复" value="fixed" />
            <el-option label="已忽略" value="ignored" />
            <el-option label="误报" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 结果列表 -->
    <div class="table-card">
      <el-table :data="resultList" stripe style="width: 100%" @row-click="showDetail">
        <el-table-column prop="id" label="风险ID" width="120" />
        <el-table-column prop="project" label="所属项目" width="160" />
        <el-table-column prop="ruleName" label="规则名称" min-width="200" />
        <el-table-column prop="level" label="严重等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="filePath" label="文件路径" min-width="200">
          <template #default="{ row }">
            <span class="file-path">{{ row.filePath }}:{{ row.line }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="firstFound" label="首次发现" width="120" />
        <el-table-column prop="lastFound" label="最近发现" width="120" />
        <el-table-column prop="status" label="修复状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click.stop="aiFix(row)">AI修复</el-button>
            <el-button link type="info" size="small" @click.stop="ignoreRisk(row)">忽略</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="风险详情" width="700px">
      <div v-if="currentRisk" class="risk-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="风险ID">{{ currentRisk.id }}</el-descriptions-item>
          <el-descriptions-item label="严重等级">
            <el-tag :type="getLevelType(currentRisk.level)">{{ currentRisk.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ currentRisk.project }}</el-descriptions-item>
          <el-descriptions-item label="规则ID">{{ currentRisk.ruleId }}</el-descriptions-item>
          <el-descriptions-item label="文件路径" :span="2">{{ currentRisk.filePath }}</el-descriptions-item>
          <el-descriptions-item label="首次发现">{{ currentRisk.firstFound }}</el-descriptions-item>
          <el-descriptions-item label="最近发现">{{ currentRisk.lastFound }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>问题代码</h4>
          <pre class="code-block">{{ currentRisk.codeSnippet }}</pre>
        </div>

        <div class="detail-section">
          <h4>规则说明</h4>
          <p>{{ currentRisk.ruleDesc }}</p>
        </div>

        <div class="detail-section">
          <h4>修复建议</h4>
          <p>{{ currentRisk.fixSuggestion }}</p>
        </div>

        <div v-if="currentRisk.aiRecord" class="detail-section">
          <h4>AI调用记录</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="调用时间">{{ currentRisk.aiRecord.time }}</el-descriptions-item>
            <el-descriptions-item label="调用Skill">{{ currentRisk.aiRecord.skill }}</el-descriptions-item>
            <el-descriptions-item label="调用结果">{{ currentRisk.aiRecord.result }}</el-descriptions-item>
            <el-descriptions-item label="采纳状态">{{ currentRisk.aiRecord.adopted ? '已采纳' : '未采纳' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="aiFix(currentRisk)">AI修复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Download } from '@element-plus/icons-vue';

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(5689);
const detailVisible = ref(false);
const currentRisk = ref<any>(null);

const filters = reactive({
  project: '',
  type: '',
  level: '',
  status: ''
});

const resultList = ref([
  { id: 'FK-20240115-001', project: '金融支付核心系统', ruleName: 'SQL注入', ruleId: 'SAST-JAVA-001', level: 'Critical', filePath: 'UserMapper.java', line: 42, firstFound: '2024-01-10', lastFound: '2024-01-15', status: 'open', statusText: '待修复', codeSnippet: 'String sql = "SELECT * FROM users WHERE id = " + userId;', ruleDesc: '该代码存在SQL注入风险，使用字符串拼接构建SQL语句。', fixSuggestion: '使用预编译语句(PreparedStatement)替代字符串拼接。', aiRecord: { time: '2024-01-15 14:32', skill: 'AI漏洞解释', result: '正常', adopted: true } },
  { id: 'FK-20240115-002', project: '用户中心服务', ruleName: 'MyBatis ${} 拼接', ruleId: 'SAST-JAVA-002', level: 'High', filePath: 'OrderMapper.xml', line: 28, firstFound: '2024-01-12', lastFound: '2024-01-15', status: 'fixing', statusText: '修复中' },
  { id: 'FK-20240115-003', project: '交易网关系统', ruleName: '硬编码密码', ruleId: 'SAST-JAVA-007', level: 'Critical', filePath: 'DbConfig.java', line: 15, firstFound: '2024-01-08', lastFound: '2024-01-15', status: 'fixed', statusText: '已修复' },
  { id: 'FK-20240115-004', project: '风控引擎服务', ruleName: 'SSRF 外部URL可控', ruleId: 'SAST-JAVA-003', level: 'High', filePath: 'HttpClient.java', line: 56, firstFound: '2024-01-14', lastFound: '2024-01-15', status: 'open', statusText: '待修复' },
  { id: 'FK-20240115-005', project: '账户管理模块', ruleName: 'Log4j 漏洞', ruleId: 'SCA-LOG4J-CVE', level: 'Critical', filePath: 'pom.xml', line: 88, firstFound: '2024-01-05', lastFound: '2024-01-15', status: 'ignored', statusText: '已忽略' },
  { id: 'FK-20240115-006', project: '订单处理系统', ruleName: 'XXE 外部实体', ruleId: 'SAST-JAVA-004', level: 'Critical', filePath: 'XmlParser.java', line: 33, firstFound: '2024-01-11', lastFound: '2024-01-15', status: 'open', statusText: '待修复' },
  { id: 'FK-20240115-007', project: '金融支付核心系统', ruleName: '敏感信息日志', ruleId: 'SAST-JAVA-011', level: 'Medium', filePath: 'LoginService.java', line: 67, firstFound: '2024-01-13', lastFound: '2024-01-15', status: 'fixed', statusText: '已修复' },
  { id: 'FK-20240115-008', project: '用户中心服务', ruleName: 'CORS 全开放', ruleId: 'SAST-JAVA-009', level: 'Medium', filePath: 'CorsConfig.java', line: 12, firstFound: '2024-01-09', lastFound: '2024-01-15', status: 'false', statusText: '误报' }
]);

function getLevelType(level: string) {
  const map: Record<string, string> = { 'Critical': 'danger', 'High': 'warning', 'Medium': 'warning', 'Low': 'info' };
  return map[level] || 'info';
}

function getStatusType(status: string) {
  const map: Record<string, string> = { 'open': 'danger', 'fixing': 'warning', 'fixed': 'success', 'ignored': 'info', 'false': 'info' };
  return map[status] || 'info';
}

function resetFilters() { Object.assign(filters, { project: '', type: '', level: '', status: '' }); }
function showDetail(row: any) { viewDetail(row); }
function viewDetail(row: any) { currentRisk.value = row; detailVisible.value = true; }
function aiFix(row: any) { console.log('AI修复', row); }
function ignoreRisk(row: any) { console.log('忽略风险', row); }
function exportReport() { console.log('导出报表'); }
</script>

<style scoped>
.results-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }
.stats-row { margin-bottom: 20px; }
.stat-mini { background: white; padding: 16px 20px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-num { font-size: 24px; font-weight: 700; color: var(--color-on-surface); }
.stat-label { font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px; }
.stat-mini.error .stat-num { color: #ef4444; }
.filter-bar { background: white; padding: 16px 20px; border-radius: 8px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.table-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.file-path { font-family: monospace; font-size: 12px; color: var(--color-on-surface-variant); }
.risk-detail h4 { font-size: 14px; font-weight: 600; margin: 20px 0 12px 0; }
.detail-section { margin-top: 16px; }
.code-block { background: #1e1e1e; color: #d4d4d4; padding: 16px; border-radius: 8px; font-size: 13px; overflow-x: auto; }
</style>
