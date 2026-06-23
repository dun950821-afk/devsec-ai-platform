<template>
  <div class="skill-page">
    <div class="page-header">
      <h2 class="page-title">大模型 Skill 中心</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新建 Skill
      </el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">128,456</span><span class="stat-label">总调用次数</span><span class="trend up">+12.5%</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">7</span><span class="stat-label">活跃Skill数</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">1.2s</span><span class="stat-label">平均响应时间</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">87.3%</span><span class="stat-label">采纳率</span><span class="trend up">+3.2%</span></div></el-col>
      </el-row>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeFilter">
        <el-radio-button label="all">全部 (7)</el-radio-button>
        <el-radio-button label="enabled">已启用 (6)</el-radio-button>
        <el-radio-button label="disabled">已停用 (1)</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Skill 卡片列表 -->
    <div class="skill-grid">
      <el-card v-for="skill in skillList" :key="skill.id" class="skill-card" shadow="hover">
        <div class="skill-header">
          <div class="skill-icon" :style="{ background: skill.iconBg }">
            <el-icon :size="24" :color="skill.iconColor"><component :is="skill.icon" /></el-icon>
          </div>
          <div class="skill-info">
            <h3>{{ skill.name }}</h3>
            <el-tag :type="skill.enabled ? 'success' : 'info'" size="small">{{ skill.enabled ? '已启用' : '已停用' }}</el-tag>
          </div>
        </div>
        <p class="skill-desc">{{ skill.description }}</p>
        <div class="skill-stats">
          <div class="stat-item">
            <span class="label">调用次数</span>
            <span class="value">{{ skill.callCount.toLocaleString() }}</span>
          </div>
          <div class="stat-item">
            <span class="label">平均耗时</span>
            <span class="value">{{ skill.avgTime }}</span>
          </div>
          <div class="stat-item">
            <span class="label">采纳率</span>
            <span class="value">{{ skill.adoptRate }}%</span>
          </div>
        </div>
        <div class="skill-actions">
          <el-button size="small" @click="configSkill(skill)">配置</el-button>
          <el-button size="small" type="primary" plain @click="testSkill(skill)">测试</el-button>
        </div>
      </el-card>
    </div>

    <!-- 配置弹窗 -->
    <el-dialog v-model="configVisible" title="Skill 配置" width="700px">
      <el-form v-if="currentSkill" :model="skillForm" label-width="100px">
        <el-form-item label="Skill名称">
          <el-input v-model="skillForm.name" />
        </el-form-item>
        <el-form-item label="调用模型">
          <el-select v-model="skillForm.model" style="width: 100%">
            <el-option label="DeepSeek-V2" value="deepseek-v2" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="智谱 GLM-4" value="glm4" />
          </el-select>
        </el-form-item>
        <el-form-item label="输入结构">
          <el-input v-model="skillForm.inputSchema" type="textarea" :rows="4" placeholder="JSON Schema" />
        </el-form-item>
        <el-form-item label="Prompt模板">
          <el-input v-model="skillForm.prompt" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="输出结构">
          <el-input v-model="skillForm.outputSchema" type="textarea" :rows="4" placeholder="JSON Schema" />
        </el-form-item>
        <el-form-item label="权限范围">
          <el-checkbox-group v-model="skillForm.permissions">
            <el-checkbox label="explain">漏洞解释</el-checkbox>
            <el-checkbox label="fix">修复建议</el-checkbox>
            <el-checkbox label="patch">Patch生成</el-checkbox>
            <el-checkbox label="report">报告生成</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, markRaw } from 'vue';
import { Plus, ChatDotRound, Setting, Operation, Warning, Document, QuestionFilled, Box } from '@element-plus/icons-vue';

const activeFilter = ref('all');
const configVisible = ref(false);
const currentSkill = ref<any>(null);

const skillForm = reactive({
  name: '',
  model: 'deepseek-v2',
  inputSchema: '',
  prompt: '',
  outputSchema: '',
  permissions: []
});

const skillList = ref([
  { id: 1, name: '漏洞解释', icon: markRaw(ChatDotRound), iconBg: '#EFF6FF', iconColor: '#2563EB', description: '调用大模型分析漏洞成因，提供风险说明、危害程度和修复思路说明', callCount: 32450, avgTime: '1.1s', adoptRate: 92.5, enabled: true },
  { id: 2, name: '修复建议', icon: markRaw(Setting), iconBg: '#F0FDF4', iconColor: '#22C55E', description: '生成安全代码示例和具体修复建议，帮助开发者快速修复风险', callCount: 28320, avgTime: '1.3s', adoptRate: 88.7, enabled: true },
  { id: 3, name: 'Patch生成', icon: markRaw(Operation), iconBg: '#FEF3C7', iconColor: '#F59E0B', description: '生成可直接应用的代码修改片段(Patch)，支持一键应用到代码', callCount: 18560, avgTime: '2.1s', adoptRate: 78.3, enabled: true },
  { id: 4, name: '误报分析', icon: markRaw(Warning), iconBg: '#FEF2F2', iconColor: '#EF4444', description: '分析检测结果是否为误报，提供误报判断依据和建议', callCount: 15680, avgTime: '0.9s', adoptRate: 95.2, enabled: true },
  { id: 5, name: '整改报告', icon: markRaw(Document), iconBg: '#F5F3FF', iconColor: '#8B5CF6', description: '生成漏洞整改报告，包含问题描述、修复方案和验证结果', callCount: 12340, avgTime: '2.5s', adoptRate: 96.8, enabled: true },
  { id: 6, name: '安全问答', icon: markRaw(QuestionFilled), iconBg: '#ECFEFF', iconColor: '#06B6D4', description: '基于安全知识库回答开发安全问题，提供安全编码建议', callCount: 11056, avgTime: '0.8s', adoptRate: 91.4, enabled: true },
  { id: 7, name: '规则生成', icon: markRaw(Box), iconBg: '#F1F5F9', iconColor: '#64748B', description: '根据企业规范自动生成检测规则，支持自定义规则模板', callCount: 10050, avgTime: '3.2s', adoptRate: 72.1, enabled: false }
]);

function showAddDialog() { console.log('新建 Skill'); }
function configSkill(skill: any) { currentSkill.value = skill; Object.assign(skillForm, skill); configVisible.value = true; }
function testSkill(skill: any) { console.log('测试 Skill', skill); }
function saveConfig() { console.log('保存配置', skillForm); configVisible.value = false; }
</script>

<style scoped>
.skill-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }
.stats-row { margin-bottom: 20px; }
.stat-mini { background: white; padding: 16px 20px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-num { font-size: 28px; font-weight: 700; color: var(--color-on-surface); }
.stat-label { font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px; }
.trend { font-size: 12px; margin-top: 4px; }
.trend.up { color: #22c55e; }
.filter-bar { margin-bottom: 20px; }
.skill-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
.skill-card { cursor: pointer; transition: all 0.2s; }
.skill-card:hover { transform: translateY(-2px); }
.skill-header { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
.skill-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.skill-info h3 { margin: 0 0 8px 0; font-size: 16px; font-weight: 600; }
.skill-desc { font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 16px 0; line-height: 1.5; }
.skill-stats { display: flex; gap: 24px; padding: 12px 0; border-top: 1px solid var(--color-outline-variant); border-bottom: 1px solid var(--color-outline-variant); margin-bottom: 16px; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .label { font-size: 11px; color: var(--color-on-surface-variant); }
.stat-item .value { font-size: 14px; font-weight: 600; }
.skill-actions { display: flex; gap: 8px; }
</style>
