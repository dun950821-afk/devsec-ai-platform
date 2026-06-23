<template>
  <div class="plugin-page">
    <div class="page-header">
      <h2 class="page-title">插件管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-mini">
            <span class="stat-num">176</span>
            <span class="stat-label">全部插件</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini online">
            <span class="stat-num">168</span>
            <span class="stat-label">在线</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini error">
            <span class="stat-num">2</span>
            <span class="stat-label">异常</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini warning">
            <span class="stat-num">6</span>
            <span class="stat-label">待升级</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filters">
        <el-form-item label="所属项目">
          <el-select v-model="filters.project" placeholder="请选择项目" clearable style="width: 180px">
            <el-option label="全部项目" value="" />
            <el-option label="金融支付核心系统" value="payment" />
            <el-option label="用户中心服务" value="user" />
            <el-option label="交易网关系统" value="gateway" />
          </el-select>
        </el-form-item>
        <el-form-item label="开发者">
          <el-select v-model="filters.developer" placeholder="请选择开发者" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="张三" value="zhangsan" />
            <el-option label="李四" value="lisi" />
            <el-option label="王五" value="wangwu" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="异常" value="error" />
            <el-option label="待升级" value="upgrade" />
          </el-select>
        </el-form-item>
        <el-form-item label="插件版本">
          <el-select v-model="filters.version" placeholder="请选择版本" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="v1.2.3" value="1.2.3" />
            <el-option label="v1.2.2" value="1.2.2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="filters.keyword" placeholder="搜索插件ID/开发者" clearable style="width: 200px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 插件列表 -->
    <div class="table-card">
      <el-table :data="pluginList" stripe style="width: 100%">
        <el-table-column prop="pluginId" label="插件ID" width="160" />
        <el-table-column prop="developer" label="开发者" width="100" />
        <el-table-column prop="project" label="所属项目" width="160" />
        <el-table-column prop="ideType" label="IDE类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.ideType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="插件版本" width="100" />
        <el-table-column prop="policy" label="当前策略" width="140" />
        <el-table-column prop="lastHeartbeat" label="最近心跳" width="160" />
        <el-table-column prop="status" label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="pushPolicy(row)">下发策略</el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary" size="small">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="disablePlugin(row)">禁用</el-dropdown-item>
                  <el-dropdown-item @click="viewLogs(row)">查看日志</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="插件详情" size="500px" direction="rtl">
      <div v-if="currentPlugin" class="plugin-detail">
        <div class="detail-section">
          <h4>基础信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="插件ID">{{ currentPlugin.pluginId }}</el-descriptions-item>
            <el-descriptions-item label="开发者">{{ currentPlugin.developer }}</el-descriptions-item>
            <el-descriptions-item label="所属项目">{{ currentPlugin.project }}</el-descriptions-item>
            <el-descriptions-item label="IDE类型">{{ currentPlugin.ideType }}</el-descriptions-item>
            <el-descriptions-item label="插件版本">{{ currentPlugin.version }}</el-descriptions-item>
            <el-descriptions-item label="当前策略">{{ currentPlugin.policy }}</el-descriptions-item>
            <el-descriptions-item label="规则包版本">{{ currentPlugin.ruleVersion }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>启用能力</h4>
          <div class="capability-list">
            <el-tag v-for="cap in currentPlugin.capabilities" :key="cap.name" size="small" :type="cap.enabled ? 'success' : 'info'" style="margin: 4px;">
              {{ cap.name }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h4>最近扫描记录</h4>
          <el-table :data="currentPlugin.scanRecords" size="small">
            <el-table-column prop="time" label="扫描时间" width="160" />
            <el-table-column prop="type" label="扫描类型" />
            <el-table-column prop="findings" label="发现问题" />
            <el-table-column prop="duration" label="耗时" width="80" />
          </el-table>
        </div>

        <div class="detail-actions">
          <el-button type="primary" @click="pushPolicy(currentPlugin)">下发策略</el-button>
          <el-button @click="viewLogs(currentPlugin)">查看日志</el-button>
          <el-button type="danger" plain @click="disablePlugin(currentPlugin)">禁用插件</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Search } from '@element-plus/icons-vue';

const filters = reactive({
  project: '',
  developer: '',
  status: '',
  version: '',
  keyword: ''
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(50);

const detailVisible = ref(false);
const currentPlugin = ref<any>(null);

const pluginList = ref([
  {
    pluginId: 'IDEA-PLG-000128',
    developer: '张三',
    project: '金融支付核心系统',
    ideType: 'IDEA 2024.1',
    version: 'v1.2.3',
    policy: '高安全性策略',
    lastHeartbeat: '2024-01-15 14:32:18',
    status: 'online',
    statusText: '在线',
    ruleVersion: '2026.01.15',
    capabilities: [
      { name: 'SCA', enabled: true },
      { name: 'SAST', enabled: true },
      { name: 'Secrets', enabled: true },
      { name: 'Baseline', enabled: true },
      { name: 'AI解释', enabled: true },
      { name: 'AI修复', enabled: true }
    ],
    scanRecords: [
      { time: '2024-01-15 14:32', type: '手动扫描', findings: 5, duration: '12s' },
      { time: '2024-01-15 13:20', type: '保存时扫描', findings: 2, duration: '8s' },
      { time: '2024-01-15 10:15', type: '提交前扫描', findings: 0, duration: '5s' }
    ]
  },
  {
    pluginId: 'IDEA-PLG-000127',
    developer: '李四',
    project: '用户中心服务',
    ideType: 'IDEA 2023.3',
    version: 'v1.2.2',
    policy: '常规开发策略',
    lastHeartbeat: '2024-01-15 14:30:45',
    status: 'online',
    statusText: '在线',
    ruleVersion: '2026.01.14'
  },
  {
    pluginId: 'IDEA-PLG-000126',
    developer: '王五',
    project: '交易网关系统',
    ideType: 'IDEA 2024.1',
    version: 'v1.2.2',
    policy: '快速扫描策略',
    lastHeartbeat: '2024-01-15 12:20:10',
    status: 'upgrade',
    statusText: '待升级',
    ruleVersion: '2026.01.10'
  },
  {
    pluginId: 'IDEA-PLG-000125',
    developer: '赵六',
    project: '风控引擎服务',
    ideType: 'IDEA 2023.2',
    version: 'v1.2.1',
    policy: '常规开发策略',
    lastHeartbeat: '2024-01-15 08:15:30',
    status: 'error',
    statusText: '异常',
    ruleVersion: '2026.01.05'
  },
  {
    pluginId: 'IDEA-PLG-000124',
    developer: '钱七',
    project: '账户管理模块',
    ideType: 'IDEA 2024.1',
    version: 'v1.2.3',
    policy: '高安全性策略',
    lastHeartbeat: '2024-01-14 18:45:22',
    status: 'offline',
    statusText: '离线',
    ruleVersion: '2026.01.15'
  }
]);

function getStatusType(status: string) {
  const map: Record<string, string> = {
    online: 'success',
    offline: 'info',
    error: 'danger',
    upgrade: 'warning'
  };
  return map[status] || 'info';
}

function resetFilters() {
  Object.assign(filters, { project: '', developer: '', status: '', version: '', keyword: '' });
}

function showDetail(row: any) {
  currentPlugin.value = row;
  detailVisible.value = true;
}

function pushPolicy(row: any) {
  console.log('下发策略', row);
}

function disablePlugin(row: any) {
  console.log('禁用插件', row);
}

function viewLogs(row: any) {
  console.log('查看日志', row);
}
</script>

<style scoped>
.plugin-page {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-mini {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-on-surface);
}

.stat-label {
  font-size: 12px;
  color: var(--color-on-surface-variant);
  margin-top: 4px;
}

.stat-mini.online .stat-num { color: #22c55e; }
.stat-mini.error .stat-num { color: #ef4444; }
.stat-mini.warning .stat-num { color: #f59e0b; }

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

.plugin-detail {
  padding: 0 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: var(--color-on-surface);
}

.capability-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-outline);
  display: flex;
  gap: 12px;
}
</style>
