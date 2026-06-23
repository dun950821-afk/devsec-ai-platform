<template>
  <div class="capability-page">
    <div class="page-header">
      <h2 class="page-title">插件能力配置</h2>
    </div>

    <!-- 策略信息 -->
    <div class="policy-info">
      <el-card>
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="info-item">
              <span class="label">当前策略</span>
              <span class="value">企业安全检测策略 v2.3</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <span class="label">适用项目</span>
              <span class="value">12 个项目</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <span class="label">规则包版本</span>
              <span class="value">2026.06.23</span>
            </div>
          </el-col>
          <el-col :span="6" class="action-col">
            <el-button @click="resetConfig">重置默认</el-button>
            <el-button type="primary" @click="saveAndPush">保存并下发</el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 能力配置卡片 -->
    <div class="capability-grid">
      <div
        v-for="cap in capabilities"
        :key="cap.id"
        class="capability-card"
        :class="{ disabled: !cap.enabled }"
      >
        <div class="cap-header">
          <div class="cap-icon" :style="{ background: cap.bgColor }">
            <el-icon :style="{ color: cap.iconColor }">
              <component :is="cap.icon" />
            </el-icon>
          </div>
          <el-switch v-model="cap.enabled" @change="onCapChange(cap)" />
        </div>
        <div class="cap-body">
          <h4 class="cap-name">{{ cap.name }}</h4>
          <p class="cap-desc">{{ cap.description }}</p>
          <div class="cap-stats">
            <span>今日扫描 {{ cap.todayCount }} 次</span>
            <span>累计 {{ cap.totalCount }} 次</span>
          </div>
        </div>
        <div class="cap-footer">
          <el-tag :type="cap.enabled ? 'success' : 'info'" size="small">
            {{ cap.enabled ? '已启用' : '已禁用' }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 右侧预览面板 -->
    <div class="preview-panel">
      <el-card class="preview-card">
        <template #header>
          <div class="preview-header">
            <span>策略下发预览</span>
          </div>
        </template>
        <div class="preview-content">
          <div class="preview-item">
            <el-icon><Folder /></el-icon>
            <span>生效项目数</span>
            <strong>12</strong>
          </div>
          <div class="preview-item">
            <el-icon><Connection /></el-icon>
            <span>生效插件数</span>
            <strong>168</strong>
          </div>
          <div class="preview-item">
            <el-icon><Clock /></el-icon>
            <span>预计下发时间</span>
            <strong>~30秒</strong>
          </div>
        </div>
      </el-card>

      <el-card class="preview-card">
        <template #header>
          <div class="preview-header">
            <span>执行状态检查</span>
          </div>
        </template>
        <div class="check-list">
          <div class="check-item success">
            <el-icon><CircleCheck /></el-icon>
            <span>策略校验通过</span>
          </div>
          <div class="check-item success">
            <el-icon><CircleCheck /></el-icon>
            <span>插件兼容性检查</span>
          </div>
          <div class="check-item success">
            <el-icon><CircleCheck /></el-icon>
            <span>规则包完整性验证</span>
          </div>
          <div class="check-item success">
            <el-icon><CircleCheck /></el-icon>
            <span>Token 权限验证</span>
          </div>
        </div>
        <el-alert
          title="当前所有检查项均通过，可安全下发"
          type="success"
          :closable="false"
          show-icon
        />
      </el-card>

      <el-card class="preview-card">
        <template #header>
          <div class="preview-header">
            <span>配置历史</span>
          </div>
        </template>
        <div class="history-list">
          <div v-for="item in history" :key="item.time" class="history-item">
            <span class="history-time">{{ item.time }}</span>
            <span class="history-desc">{{ item.desc }}</span>
            <span class="history-user">{{ item.user }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 保存确认弹窗 -->
    <el-dialog v-model="confirmVisible" title="确认下发" width="400px">
      <p>确认要将配置下发到所有关联插件吗？</p>
      <p style="color: var(--color-on-surface-variant); font-size: 12px; margin-top: 8px;">
        此操作将影响 12 个项目下的 168 个插件实例
      </p>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPush">确认下发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Folder,
  Connection,
  Clock,
  CircleCheck,
  Box,
  Lock,
  Key,
  Document,
  User,
  ChatDotSquare,
  MagicStick,
  Position,
  WarningFilled
} from '@element-plus/icons-vue';

const confirmVisible = ref(false);

const capabilities = ref([
  {
    id: 'sca',
    name: 'SCA 开源组件检测',
    description: '检测 Maven、Gradle 等依赖中的漏洞组件、许可证风险',
    icon: Box,
    bgColor: '#DBEAFE',
    iconColor: '#8B5CF6',
    enabled: true,
    todayCount: 456,
    totalCount: '12.3k'
  },
  {
    id: 'sast',
    name: 'SAST 代码安全检测',
    description: '静态代码安全扫描，检测 SQL 注入、XSS、命令执行等',
    icon: Lock,
    bgColor: '#DBEAFE',
    iconColor: '#3B82F6',
    enabled: true,
    todayCount: 892,
    totalCount: '28.6k'
  },
  {
    id: 'secrets',
    name: 'Secrets 敏感信息检测',
    description: '识别 AK/SK、Token、私钥、数据库口令等敏感信息',
    icon: Key,
    bgColor: '#FEE2E2',
    iconColor: '#EF4444',
    enabled: true,
    todayCount: 234,
    totalCount: '5.2k'
  },
  {
    id: 'baseline',
    name: 'Baseline 企业基线检测',
    description: '检测企业安全编码规范、Spring Boot 配置、日志脱敏等',
    icon: Document,
    bgColor: '#FEF3C7',
    iconColor: '#F59E0B',
    enabled: true,
    todayCount: 156,
    totalCount: '3.8k'
  },
  {
    id: 'aiExplain',
    name: 'AI 漏洞解释',
    description: '大模型漏洞成因分析，帮助开发理解安全问题',
    icon: User,
    bgColor: '#DCFCE7',
    iconColor: '#22C55E',
    enabled: true,
    todayCount: 328,
    totalCount: '8.9k'
  },
  {
    id: 'aiFix',
    name: 'AI 修复建议',
    description: '生成安全代码示例和 Patch 建议，辅助快速修复',
    icon: MagicStick,
    bgColor: '#DCFCE7',
    iconColor: '#06B6D4',
    enabled: true,
    todayCount: 245,
    totalCount: '6.2k'
  },
  {
    id: 'commitCheck',
    name: 'Git Commit 提交前检查',
    description: '提交前自动执行安全扫描，拦截高危风险',
    icon: Position,
    bgColor: '#DBEAFE',
    iconColor: '#8B5CF6',
    enabled: false,
    todayCount: 0,
    totalCount: '1.5k'
  },
  {
    id: 'blockCommit',
    name: '高危风险阻断提交',
    description: 'Critical/High 风险禁止 Git 提交，强制修复后放行',
    icon: WarningFilled,
    bgColor: '#FEE2E2',
    iconColor: '#EC4899',
    enabled: false,
    todayCount: 0,
    totalCount: '890'
  }
]);

const history = ref([
  { time: '2024-01-15 14:32', desc: '启用 AI 修复建议', user: '张三' },
  { time: '2024-01-15 10:15', desc: '调整阻断等级配置', user: '李四' },
  { time: '2024-01-14 16:45', desc: '启用 Secrets 检测', user: '张三' },
  { time: '2024-01-13 09:30', desc: '初始化策略配置', user: '王五' }
]);

function onCapChange(cap: any) {
  console.log('能力开关变化', cap);
}

function resetConfig() {
  ElMessage.info('已重置为默认配置');
}

function saveAndPush() {
  confirmVisible.value = true;
}

function confirmPush() {
  confirmVisible.value = false;
  ElMessage.success('策略已下发');
}
</script>

<style scoped>
.capability-page {
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

.policy-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: var(--color-on-surface-variant);
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-on-surface);
}

.action-col {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 能力卡片 */
.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.capability-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.2s;
}

.capability-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.capability-card.disabled {
  opacity: 0.7;
}

.cap-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.cap-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.cap-body {
  margin-bottom: 12px;
}

.cap-name {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.cap-desc {
  font-size: 12px;
  color: var(--color-on-surface-variant);
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.cap-stats {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-on-surface-variant);
}

.cap-footer {
  padding-top: 12px;
  border-top: 1px solid var(--color-outline);
}

/* 预览面板 */
.preview-panel {
  position: fixed;
  right: 40px;
  top: 180px;
  width: 320px;
}

.preview-card {
  margin-bottom: 16px;
}

.preview-header {
  font-weight: 600;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.preview-item span {
  flex: 1;
  color: var(--color-on-surface-variant);
}

.preview-item strong {
  color: var(--color-primary);
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.check-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.check-item.success {
  color: #22c55e;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
}

.history-time {
  color: var(--color-on-surface-variant);
}

.history-desc {
  color: var(--color-on-surface);
}

.history-user {
  color: var(--color-primary);
}
</style>
