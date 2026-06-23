<template>
  <div class="home-page">
    <div class="page-header">
      <h2 class="page-title">平台运行总览</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
        />
        <el-button type="primary" @click="loadDashboard">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="goToPage('/project')">
        <div class="stat-icon" style="background: #DBEAFE;">
          <el-icon style="color: #2563EB;"><Folder /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.totalProjects }}</div>
          <div class="stat-label">接入项目</div>
        </div>
        <div class="stat-trend" :class="dashboardData.activeProjects > 0 ? 'up' : ''">
          <el-icon><Top /></el-icon>
          <span>{{ dashboardData.activeProjects }} 活跃</span>
        </div>
      </div>

      <div class="stat-card" @click="goToPage('/plugin')">
        <div class="stat-icon" style="background: #DCFCE7;">
          <el-icon style="color: #22c55e;"><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.totalPlugins }}</div>
          <div class="stat-label">在线插件</div>
        </div>
        <div class="stat-trend" :class="dashboardData.activePlugins > 0 ? 'up' : ''">
          <el-icon><Top /></el-icon>
          <span>{{ dashboardData.activePlugins }} 在线</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #FEF3C7;">
          <el-icon style="color: #f59e0b;"><Search /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.todayScans }}</div>
          <div class="stat-label">今日扫描</div>
        </div>
      </div>

      <div class="stat-card danger" @click="goToPage('/results')">
        <div class="stat-icon" style="background: #FEE2E2;">
          <el-icon style="color: #ef4444;"><WarningFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ dashboardData.highIssues + dashboardData.criticalIssues }}</div>
          <div class="stat-label">高危风险</div>
        </div>
        <div class="stat-trend down" v-if="dashboardData.criticalIssues > 0">
          <span>{{ dashboardData.criticalIssues }} 严重</span>
        </div>
      </div>
    </div>

    <!-- 问题分布卡片 -->
    <div class="stats-grid secondary">
      <div class="stat-card small" style="border-left: 3px solid #ef4444;">
        <div class="stat-content">
          <div class="stat-value" style="color: #ef4444;">{{ dashboardData.criticalIssues }}</div>
          <div class="stat-label">严重</div>
        </div>
      </div>
      <div class="stat-card small" style="border-left: 3px solid #f97316;">
        <div class="stat-content">
          <div class="stat-value" style="color: #f97316;">{{ dashboardData.highIssues }}</div>
          <div class="stat-label">高危</div>
        </div>
      </div>
      <div class="stat-card small" style="border-left: 3px solid #eab308;">
        <div class="stat-content">
          <div class="stat-value" style="color: #eab308;">{{ dashboardData.mediumIssues }}</div>
          <div class="stat-label">中危</div>
        </div>
      </div>
      <div class="stat-card small" style="border-left: 3px solid #22c55e;">
        <div class="stat-content">
          <div class="stat-value" style="color: #22c55e;">{{ dashboardData.lowIssues }}</div>
          <div class="stat-label">低危</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 风险趋势图 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>风险趋势</h3>
          <div class="chart-legend">
            <span class="legend-item"><i class="dot" style="background: #8B5CF6;"></i>SCA</span>
            <span class="legend-item"><i class="dot" style="background: #3B82F6;"></i>SAST</span>
            <span class="legend-item"><i class="dot" style="background: #EF4444;"></i>Secrets</span>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <!-- 插件状态饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>插件运行状态</h3>
        </div>
        <div ref="pieChartRef" class="chart-container small"></div>
      </div>

      <!-- AI Skill 调用统计 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>AI Skill 调用统计</h3>
        </div>
        <div ref="barChartRef" class="chart-container small"></div>
      </div>
    </div>

    <!-- 最近检测结果 -->
    <div class="recent-results">
      <div class="section-header">
        <h3>最近检测结果</h3>
        <el-button text type="primary" @click="goToPage('/results')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentResults" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="policyName" label="策略名称" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalIssues" label="问题数" width="80" />
        <el-table-column prop="createTime" label="扫描时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import {
  Folder,
  Connection,
  Search,
  WarningFilled,
  Refresh,
  Top,
  Bottom,
  ArrowRight
} from '@element-plus/icons-vue';
import { dashboardApi } from '@/services/api';

const router = useRouter();
const dateRange = ref<[Date, Date] | null>(null);
const trendChartRef = ref<HTMLElement>();
const pieChartRef = ref<HTMLElement>();
const barChartRef = ref<HTMLElement>();
const recentResults = ref<any[]>([]);

let trendChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;
let barChart: echarts.ECharts | null = null;

const dashboardData = reactive({
  totalProjects: 0,
  activeProjects: 0,
  totalPlugins: 0,
  activePlugins: 0,
  todayScans: 0,
  totalIssues: 0,
  criticalIssues: 0,
  highIssues: 0,
  mediumIssues: 0,
  lowIssues: 0
});

async function loadDashboard() {
  try {
    const data = await dashboardApi.getStats();
    Object.assign(dashboardData, data);
    updateCharts();
  } catch (error) {
    console.error('Failed to load dashboard:', error);
    ElMessage.error('加载仪表盘数据失败');
  }
}

async function loadRecentResults() {
  try {
    const { resultApi } = await import('@/services/api');
    const data = await resultApi.list({ current: 1, size: 5 });
    if (data.records) {
      recentResults.value = data.records.map((item: any) => ({
        id: item.id,
        projectName: item.projectId ? `项目 #${item.projectId}` : '-',
        policyName: item.policyId ? `策略 #${item.policyId}` : '-',
        status: item.status,
        totalIssues: item.totalIssues || 0,
        createTime: item.createTime || '-'
      }));
    }
  } catch (error) {
    console.error('Failed to load recent results:', error);
  }
}

function updateCharts() {
  // 更新饼图数据
  if (pieChart) {
    pieChart.setOption({
      series: [{
        data: [
          { value: dashboardData.activePlugins, name: '在线', itemStyle: { color: '#22c55e' } },
          { value: Math.max(0, dashboardData.totalPlugins - dashboardData.activePlugins), name: '离线', itemStyle: { color: '#9CA3AF' } }
        ]
      }]
    });
  }

  // 更新柱状图数据
  if (barChart) {
    barChart.setOption({
      series: [{
        data: [
          dashboardData.todayScans,
          dashboardData.totalIssues,
          dashboardData.criticalIssues,
          dashboardData.highIssues
        ]
      }]
    });
  }
}

function goToPage(path: string) {
  router.push(path);
}

function viewDetail(row: any) {
  router.push(`/results?id=${row.id}`);
}

function getStatusType(status: number) {
  const types: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  };
  return types[status] || 'info';
}

function getStatusText(status: number) {
  const texts: Record<number, string> = {
    0: '待扫描',
    1: '扫描中',
    2: '已完成',
    3: '失败'
  };
  return texts[status] || '未知';
}

function initTrendChart() {
  if (!trendChartRef.value) return;
  
  trendChart = echarts.init(trendChartRef.value);
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { show: false },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['1/9', '1/10', '1/11', '1/12', '1/13', '1/14', '1/15'],
      axisLine: { lineStyle: { color: '#E5E7EB' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F3F4F6' } }
    },
    series: [
      { name: 'SCA', type: 'line', data: [45, 52, 48, 60, 55, 42, 38], smooth: true, color: '#8B5CF6' },
      { name: 'SAST', type: 'line', data: [28, 35, 42, 38, 45, 52, 48], smooth: true, color: '#3B82F6' },
      { name: 'Secrets', type: 'line', data: [12, 15, 18, 14, 20, 16, 12], smooth: true, color: '#EF4444' }
    ]
  };
  trendChart.setOption(option);
}

function initPieChart() {
  if (!pieChartRef.value) return;
  
  pieChart = echarts.init(pieChartRef.value);
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 20, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [
        { value: dashboardData.activePlugins || 0, name: '在线', itemStyle: { color: '#22c55e' } },
        { value: Math.max(0, (dashboardData.totalPlugins || 0) - (dashboardData.activePlugins || 0)), name: '离线', itemStyle: { color: '#9CA3AF' } }
      ]
    }]
  };
  pieChart.setOption(option);
}

function initBarChart() {
  if (!barChartRef.value) return;
  
  barChart = echarts.init(barChartRef.value);
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['今日扫描', '总问题', '严重', '高危'],
      axisLine: { lineStyle: { color: '#E5E7EB' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F3F4F6' } }
    },
    series: [{
      type: 'bar',
      data: [
        dashboardData.todayScans,
        dashboardData.totalIssues,
        dashboardData.criticalIssues,
        dashboardData.highIssues
      ],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3B82F6' },
          { offset: 1, color: '#60A5FA' }
        ])
      },
      barWidth: 40
    }]
  };
  barChart.setOption(option);
}

function handleResize() {
  trendChart?.resize();
  pieChart?.resize();
  barChart?.resize();
}

onMounted(async () => {
  await loadDashboard();
  loadRecentResults();
  
  initTrendChart();
  initPieChart();
  initBarChart();
  
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  trendChart?.dispose();
  pieChart?.dispose();
  barChart?.dispose();
});
</script>

<style scoped>
.home-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stats-grid.secondary {
  grid-template-columns: repeat(4, 1fr);
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card.small {
  padding: 16px;
  cursor: default;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.stat-trend.up {
  color: #22c55e;
}

.stat-trend.down {
  color: #ef4444;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.chart-container {
  height: 280px;
}

.chart-container.small {
  height: 220px;
}

.recent-results {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}
</style>
