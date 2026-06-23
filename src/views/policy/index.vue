<template>
  <div class="policy-page">
    <div class="page-header">
      <h2 class="page-title">检测策略中心</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新建策略
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">8</span><span class="stat-label">策略总数</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">6</span><span class="stat-label">已启用</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">24</span><span class="stat-label">已绑定项目</span></div></el-col>
        <el-col :span="6"><div class="stat-mini"><span class="stat-num">v3.2</span><span class="stat-label">策略版本</span></div></el-col>
      </el-row>
    </div>

    <!-- 策略列表 -->
    <div class="table-card">
      <el-table :data="policyList" stripe style="width: 100%">
        <el-table-column prop="name" label="策略名称" min-width="180">
          <template #default="{ row }">
            <div class="policy-name">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.enabled" type="success" size="small">已启用</el-tag>
              <el-tag v-else type="info" size="small">未启用</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="scope" label="适用范围" width="120" />
        <el-table-column prop="scanTypes" label="扫描方式" width="200">
          <template #default="{ row }">
            <el-tag v-for="type in row.scanTypes" :key="type" size="small" style="margin-right: 4px;">{{ type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="blockLevel" label="阻断等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.blockLevel)" size="small">{{ row.blockLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ruleVersion" label="规则包版本" width="120" />
        <el-table-column prop="updateTime" label="最近更新" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editPolicy(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="showVersion(row)">版本</el-button>
            <el-button link type="danger" size="small" @click="deletePolicy(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑策略' : '新建策略'" width="600px">
      <el-form :model="policyForm" label-width="120px">
        <el-form-item label="策略名称" required>
          <el-input v-model="policyForm.name" placeholder="请输入策略名称" />
        </el-form-item>
        <el-form-item label="适用范围">
          <el-select v-model="policyForm.scope" placeholder="请选择" style="width: 100%">
            <el-option label="全部项目" value="all" />
            <el-option label="高安全项目" value="high" />
            <el-option label="常规项目" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="扫描触发方式">
          <el-checkbox-group v-model="policyForm.scanTypes">
            <el-checkbox label="手动扫描">手动扫描</el-checkbox>
            <el-checkbox label="保存时">保存时</el-checkbox>
            <el-checkbox label="依赖变更">依赖变更</el-checkbox>
            <el-checkbox label="提交前">提交前</el-checkbox>
            <el-checkbox label="定时扫描">定时扫描</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="扫描范围">
          <el-select v-model="policyForm.scanScope" placeholder="请选择" style="width: 100%">
            <el-option label="整个项目" value="project" />
            <el-option label="当前模块" value="module" />
            <el-option label="当前文件" value="file" />
          </el-select>
        </el-form-item>
        <el-form-item label="阻断规则">
          <el-checkbox-group v-model="policyForm.blockLevels">
            <el-checkbox label="Critical">Critical</el-checkbox>
            <el-checkbox label="High">High</el-checkbox>
            <el-checkbox label="Medium">Medium</el-checkbox>
            <el-checkbox label="Low">Low</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="AI调用策略">
          <el-switch v-model="policyForm.aiExplain" /> 漏洞解释
          <el-switch v-model="policyForm.aiFix" /> 修复建议
          <el-switch v-model="policyForm.aiPatch" /> Patch生成
          <el-switch v-model="policyForm.aiReport" /> 报告生成
        </el-form-item>
        <el-form-item label="策略描述">
          <el-input v-model="policyForm.desc" type="textarea" :rows="3" placeholder="请输入策略描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePolicy">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Plus } from '@element-plus/icons-vue';

const dialogVisible = ref(false);
const isEdit = ref(false);

const policyForm = reactive({
  name: '',
  scope: '',
  scanTypes: ['手动扫描', '保存时'],
  scanScope: 'project',
  blockLevels: ['Critical', 'High'],
  aiExplain: true,
  aiFix: true,
  aiPatch: false,
  aiReport: true,
  desc: ''
});

const policyList = ref([
  { name: '高安全性项目策略', scope: '高安全项目', scanTypes: ['手动扫描', '保存时', '依赖变更', '提交前'], blockLevel: '严重', ruleVersion: 'v2024.06.23', enabled: true, updateTime: '2024-01-15 14:30' },
  { name: '常规开发策略', scope: '常规项目', scanTypes: ['手动扫描', '保存时'], blockLevel: '高危', ruleVersion: 'v2024.06.23', enabled: true, updateTime: '2024-01-15 10:20' },
  { name: '快速扫描策略', scope: '常规项目', scanTypes: ['手动扫描'], blockLevel: '仅提醒', ruleVersion: 'v2024.06.23', enabled: true, updateTime: '2024-01-14 16:45' },
  { name: 'CI/CD集成策略', scope: '全部项目', scanTypes: ['定时扫描'], blockLevel: '高危', ruleVersion: 'v2024.06.23', enabled: true, updateTime: '2024-01-13 09:30' },
  { name: '移动端专项策略', scope: '移动端', scanTypes: ['手动扫描', '提交前'], blockLevel: '高危', ruleVersion: 'v2024.06.23', enabled: true, updateTime: '2024-01-12 15:00' },
  { name: '前端项目策略', scope: '前端项目', scanTypes: ['手动扫描', '提交前'], blockLevel: '中危', ruleVersion: 'v2024.06.20', enabled: false, updateTime: '2024-01-10 11:20' }
]);

function getLevelType(level: string) {
  const map: Record<string, string> = { '严重': 'danger', '高危': 'warning', '中危': 'warning', '仅提醒': 'info' };
  return map[level] || 'info';
}

function showAddDialog() {
  isEdit.value = false;
  Object.assign(policyForm, { name: '', scope: '', scanTypes: ['手动扫描', '保存时'], scanScope: 'project', blockLevels: ['Critical', 'High'], aiExplain: true, aiFix: true, aiPatch: false, aiReport: true, desc: '' });
  dialogVisible.value = true;
}

function editPolicy(row: any) {
  isEdit.value = true;
  Object.assign(policyForm, row);
  dialogVisible.value = true;
}

function showVersion(row: any) { console.log('查看版本', row); }
function deletePolicy(row: any) { console.log('删除策略', row); }
function savePolicy() { console.log('保存策略', policyForm); dialogVisible.value = false; }
</script>

<style scoped>
.policy-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }
.stats-row { margin-bottom: 20px; }
.stat-mini { background: white; padding: 16px 20px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-num { font-size: 28px; font-weight: 700; color: var(--color-on-surface); }
.stat-label { font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px; }
.table-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.policy-name { display: flex; align-items: center; gap: 8px; }
</style>
