<template>
  <div class="audit-page">
    <div class="page-header">
      <h2 class="page-title">插件审计日志</h2>
      <el-button type="primary" @click="exportLogs">
        <el-icon><Download /></el-icon>
        导出日志
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filters">
        <el-form-item label="操作类型">
          <el-select v-model="filters.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="安全扫描" value="scan" />
            <el-option label="策略下发" value="policy" />
            <el-option label="AI调用" value="ai" />
            <el-option label="启用插件" value="enable" />
            <el-option label="禁用插件" value="disable" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属项目">
          <el-select v-model="filters.project" placeholder="请选择" clearable style="width: 160px">
            <el-option label="全部项目" value="" />
            <el-option label="金融支付核心系统" value="payment" />
            <el-option label="用户中心服务" value="user" />
            <el-option label="交易网关系统" value="gateway" />
            <el-option label="风控引擎服务" value="risk" />
          </el-select>
        </el-form-item>
        <el-form-item label="开发者">
          <el-select v-model="filters.developer" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="张三" value="zhangsan" />
            <el-option label="李四" value="lisi" />
            <el-option label="王五" value="wangwu" />
            <el-option label="赵六" value="zhaoliu" />
          </el-select>
        </el-form-item>
        <el-form-item label="插件ID">
          <el-input v-model="filters.pluginId" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filters.level" placeholder="请选择" clearable style="width: 100px">
            <el-option label="全部" value="" />
            <el-option label="Critical" value="critical" />
            <el-option label="High" value="high" />
            <el-option label="Medium" value="medium" />
            <el-option label="Low" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-select v-model="filters.timeRange" placeholder="请选择" clearable style="width: 120px">
            <el-option label="今天" value="today" />
            <el-option label="昨天" value="yesterday" />
            <el-option label="近7天" value="week" />
            <el-option label="近30天" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table :data="logList" stripe style="width: 100%" @row-click="showDetail">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="user" label="操作用户" width="100" />
        <el-table-column prop="pluginId" label="插件ID" width="150" />
        <el-table-column prop="project" label="所属项目" width="160" />
        <el-table-column prop="actionType" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.actionType)" size="small">
              {{ row.actionTypeText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="200" />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small">
              {{ row.resultText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="来源IP" width="130" />
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
    <el-dialog v-model="detailVisible" title="审计详情" width="700px">
      <div v-if="currentLog" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作时间">{{ currentLog.time }}</el-descriptions-item>
          <el-descriptions-item label="操作用户">{{ currentLog.user }}</el-descriptions-item>
          <el-descriptions-item label="插件ID">{{ currentLog.pluginId }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ currentLog.project }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ currentLog.actionTypeText }}</el-descriptions-item>
          <el-descriptions-item label="来源IP">{{ currentLog.ip }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentLog.aiDetail" class="ai-detail">
          <h4>AI 调用详情</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="调用模型">{{ currentLog.aiDetail.model }}</el-descriptions-item>
            <el-descriptions-item label="Token 消耗">{{ currentLog.aiDetail.tokens }}</el-descriptions-item>
            <el-descriptions-item label="调用结果">{{ currentLog.aiDetail.result }}</el-descriptions-item>
            <el-descriptions-item label="是否采纳">{{ currentLog.aiDetail.adopted ? '是' : '否' }}</el-descriptions-item>
          </el-descriptions>
          <div class="ai-content">
            <div class="ai-section">
              <h5>输入 Prompt</h5>
              <div class="content-box">{{ currentLog.aiDetail.input }}</div>
            </div>
            <div class="ai-section">
              <h5>AI 输出</h5>
              <div class="content-box">{{ currentLog.aiDetail.output }}</div>
            </div>
          </div>
        </div>

        <div v-if="currentLog.riskDetail" class="risk-detail">
          <h4>关联风险项</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="CWE 编号">{{ currentLog.riskDetail.cwe }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">{{ currentLog.riskDetail.level }}</el-descriptions-item>
            <el-descriptions-item label="文件位置">{{ currentLog.riskDetail.file }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Download } from '@element-plus/icons-vue';

const filters = reactive({
  type: '',
  project: '',
  developer: '',
  pluginId: '',
  level: '',
  timeRange: 'week'
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(100);

const detailVisible = ref(false);
const currentLog = ref<any>(null);

const logList = ref([
  {
    time: '2024-01-15 14:32:18',
    user: '张三',
    pluginId: 'IDEA-PLG-000128',
    project: '金融支付核心系统',
    actionType: 'ai',
    actionTypeText: 'AI调用',
    detail: '调用 AI 漏洞解释，分析 SQL 注入风险',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.105',
    aiDetail: {
      model: 'DeepSeek-V2',
      tokens: '2,340',
      result: '正常',
      adopted: true,
      input: '请解释以下 SQL 注入风险的成因和修复方案...',
      output: '该代码存在 SQL 注入风险，因为在字符串拼接中使用了用户输入...'
    },
    riskDetail: {
      cwe: 'CWE-89',
      level: 'High',
      file: 'UserMapper.java:42'
    }
  },
  {
    time: '2024-01-15 14:28:45',
    user: '李四',
    pluginId: 'IDEA-PLG-000127',
    project: '用户中心服务',
    actionType: 'scan',
    actionTypeText: '安全扫描',
    detail: '手动扫描当前文件，发现 3 个风险项',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.108'
  },
  {
    time: '2024-01-15 14:20:12',
    user: '王五',
    pluginId: 'IDEA-PLG-000126',
    project: '交易网关系统',
    actionType: 'policy',
    actionTypeText: '策略下发',
    detail: '下发新策略：企业安全检测策略 v2.3',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.112'
  },
  {
    time: '2024-01-15 13:45:33',
    user: '张三',
    pluginId: 'IDEA-PLG-000128',
    project: '金融支付核心系统',
    actionType: 'ai',
    actionTypeText: 'AI调用',
    detail: '调用 AI 修复建议，生成 Patch 代码',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.105',
    aiDetail: {
      model: 'DeepSeek-V2',
      tokens: '3,560',
      result: '正常',
      adopted: false,
      input: '请为以下 SQL 注入风险生成修复代码...',
      output: '建议使用预编译语句替代字符串拼接...'
    }
  },
  {
    time: '2024-01-15 13:30:20',
    user: '赵六',
    pluginId: 'IDEA-PLG-000125',
    project: '风控引擎服务',
    actionType: 'scan',
    actionTypeText: '安全扫描',
    detail: '提交前扫描，检测到 High 风险，阻断提交',
    result: 'blocked',
    resultText: '已阻断',
    ip: '192.168.1.115',
    riskDetail: {
      cwe: 'CWE-22',
      level: 'High',
      file: 'FileController.java:78'
    }
  },
  {
    time: '2024-01-15 12:15:08',
    user: '钱七',
    pluginId: 'IDEA-PLG-000124',
    project: '账户管理模块',
    actionType: 'enable',
    actionTypeText: '启用插件',
    detail: '插件启动成功，握手完成',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.120'
  },
  {
    time: '2024-01-15 11:45:55',
    user: '系统',
    pluginId: 'IDEA-PLG-000123',
    project: '核心交易系统',
    actionType: 'policy',
    actionTypeText: '策略下发',
    detail: '批量下发规则包更新：2026.06.23',
    result: 'partial',
    resultText: '部分失败',
    ip: '192.168.1.1'
  },
  {
    time: '2024-01-15 10:30:42',
    user: '李四',
    pluginId: 'IDEA-PLG-000127',
    project: '用户中心服务',
    actionType: 'ai',
    actionTypeText: 'AI调用',
    detail: '调用误报分析，判断是否为误报',
    result: 'success',
    resultText: '成功',
    ip: '192.168.1.108'
  }
]);

function getTypeColor(type: string) {
  const map: Record<string, string> = {
    ai: 'primary',
    scan: 'warning',
    policy: 'info',
    enable: 'success',
    disable: 'danger'
  };
  return map[type] || 'info';
}

function resetFilters() {
  Object.assign(filters, { type: '', project: '', developer: '', pluginId: '', level: '', timeRange: 'week' });
}

function showDetail(row: any) {
  currentLog.value = row;
  detailVisible.value = true;
}

function exportLogs() {
  console.log('导出日志');
}
</script>

<style scoped>
.audit-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.table-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 20px 0 12px 0;
}

.ai-detail, .risk-detail {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--color-outline);
}

.ai-content {
  margin-top: 12px;
}

.ai-section {
  margin-bottom: 12px;
}

.ai-section h5 {
  font-size: 12px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--color-on-surface-variant);
}

.content-box {
  background: var(--color-surface-container);
  padding: 12px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
}
</style>
