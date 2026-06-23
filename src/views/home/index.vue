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
        <el-button type="primary">
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
          <div class="stat-value">42</div>
          <div class="stat-label">接入项目</div>
        </div>
        <div class="stat-trend up">
          <el-icon><Top /></el-icon>
          <span>12%</span>
        </div>
      </div>

      <div class="stat-card" @click="goToPage('/plugin')">
        <div class="stat-icon" style="background: #DCFCE7;">
          <el-icon style="color: #22c55e;"><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">168</div>
          <div class="stat-label">在线插件</div>
        </div>
        <div class="stat-trend up">
          <el-icon><Top /></el-icon>
          <span>8%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #FEF3C7;">
          <el-icon style="color: #f59e0b;"><Search /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">1,268</div>
          <div class="stat-label">今日扫描</div>
        </div>
        <div class="stat-trend down">
          <el-icon><Bottom /></el-icon>
          <span>5%</span>
        </div>
      </div>

      <div class="stat-card danger" @click="goToPage('/results')">
        <div class="stat-icon" style="background: #FEE2E2;">
          <el-icon style="color: #ef4444;"><WarningFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">23</div>
          <div class="stat-label">高危风险</div>
        </div>
        <div class="stat-trend down">
          <el-icon><Bottom /></el-icon>
          <span>15%</span>
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
            <span class="legend-item"><i class="dot" style="background: #F59E0B;"></i>Baseline</span>
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

    <!-- Top 风险项目 -->
    <div class="risk-projects">
      <div class="section-header">
        <h3>Top 风险项目</h3>
        <el-button text type="primary" @click="goToPage('/project')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="topProjects" stripe style="width: 100%">
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="owner" label="负责人" width="120" />
        <el-table-column prop="critical" label="高危" width="80">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.critical }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="high" label="中危" width="80">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.high }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="low" label="低危" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ row.low }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastScan" label="最近扫描" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
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

const router = useRouter();
const dateRange = ref<[Date, Date] | null>(null);
const trendChartRef = ref<HTMLElement>();
const pieChartRef = ref<HTMLElement>();
const barChartRef = ref<HTMLElement>();

let trendChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;
let barChart: echarts.ECharts | null = null;

const topProjects = ref([
  { name: '金融支付核心系统', owner: '张明', critical: 5, high: 12, low: 28, lastScan: '2024-01-15 14:32' },
  { name: '用户中心服务', owner: '李华', critical: 3, high: 8, low: 15, lastScan: '2024-01-15 13:20' },
  { name: '交易网关系统', owner: '王芳', critical: 2, high: 6, low: 10, lastScan: '2024-01-15 12:45' },
  { name: '风控引擎服务', owner: '赵强', critical: 1, high: 4, low: 8, lastScan: '2024-01-15 11:30' },
  { name: '账户管理模块', owner: '钱伟', critical: 1, high: 3, low: 6, lastScan: '2024-01-15 10:15' }
]);

function goToPage(path: string) {
  router.push(path);
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
      { name: 'Secrets', type: 'line', data: [12, 15, 18, 14, 20, 16, 12], smooth: true, color: '#EF4444' },
      { name: 'Baseline', type: 'line', data: [20, 25, 22, 28, 30, 26, 24], smooth: true, color: '#F59E0B' }
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
        { value: 168, name: '在线', itemStyle: { color: '#22c55e' } },
        { value: 6, name: '离线', itemStyle: { color: '#9CA3AF' } },
        { value: 2, name: '异常', itemStyle: { color: '#EF4444' } }
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
      data: ['1/9', '1/10', '1/11', '1/12', '1/13', '1/14', '1/15'],
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
        { value: 120, itemStyle: { color: '#8B5CF6' } },
        { value: 145, itemStyle: { color: '#3B82F6' } },
        { value: 98, itemStyle: { color: '#22c55e' } },
        { value: 156, itemStyle: { color: '#F59E0B' } },
        { value: 132, itemStyle: { color: '#EF4444' } },
        { value: 168, itemStyle: { color: '#EC4899' } },
        { value: 142, itemStyle: { color: '#06B6D4' } }
      ],
      barWidth: '40%',
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  };
  barChart.setOption(option);
}

function handleResize() {
  trendChart?.resize();
  pieChart?.resize();
  barChart?.resize();
}

onMounted(() => {
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
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-on-surface);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--color-on-surface-variant);
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.stat-trend.up {
  color: #22c55e;
}

.stat-trend.down {
  color: #ef4444;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
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
  color: var(--color-on-surface-variant);
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
  height: 240px;
}

/* Top 风险项目 */
.risk-projects {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
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
