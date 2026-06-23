<template>
  <div class="project-page">
    <div class="page-header">
      <h2 class="page-title">项目管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新增项目
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-mini">
            <span class="stat-num">24</span>
            <span class="stat-label">项目总数</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini online">
            <span class="stat-num">168</span>
            <span class="stat-label">在线插件数</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini error">
            <span class="stat-num">15</span>
            <span class="stat-label">高危风险数</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-mini">
            <span class="stat-num">87.5</span>
            <span class="stat-label">平均安全评分</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 项目列表 -->
    <div class="table-card">
      <el-table :data="projectList" stripe style="width: 100%">
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="department" label="所属部门" width="120" />
        <el-table-column prop="owner" label="项目负责人" width="100" />
        <el-table-column prop="gitUrl" label="Git地址" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" :href="row.gitUrl" target="_blank" class="git-link">
              {{ row.gitUrl }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="policy" label="绑定策略" width="140" />
        <el-table-column prop="pluginCount" label="在线插件" width="90" align="center" />
        <el-table-column label="风险" width="120">
          <template #default="{ row }">
            <span class="risk-badge critical">{{ row.critical }} 高</span>
            <span class="risk-badge high">{{ row.high }} 中</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastScan" label="最近扫描" width="160" />
        <el-table-column prop="score" label="安全评分" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.score)" size="small">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editProject(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="bindPolicy(row)">绑定策略</el-button>
            <el-button link type="danger" size="small" @click="deleteProject(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新增项目'" width="500px">
      <el-form :model="projectForm" label-width="100px">
        <el-form-item label="项目名称" required>
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="projectForm.department" placeholder="请输入所属部门" />
        </el-form-item>
        <el-form-item label="项目负责人">
          <el-input v-model="projectForm.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="Git地址">
          <el-input v-model="projectForm.gitUrl" placeholder="请输入Git仓库地址" />
        </el-form-item>
        <el-form-item label="绑定策略">
          <el-select v-model="projectForm.policy" placeholder="请选择策略" style="width: 100%">
            <el-option label="高安全性策略" value="high" />
            <el-option label="常规开发策略" value="normal" />
            <el-option label="快速扫描策略" value="fast" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProject">保存</el-button>
      </template>
    </el-dialog>

    <!-- 绑定策略弹窗 -->
    <el-dialog v-model="policyDialogVisible" title="绑定策略" width="400px">
      <el-form :model="policyForm" label-width="100px">
        <el-form-item label="当前项目">
          <span>{{ currentProject?.name }}</span>
        </el-form-item>
        <el-form-item label="选择策略" required>
          <el-select v-model="policyForm.policyId" placeholder="请选择策略" style="width: 100%">
            <el-option label="高安全性策略" value="high" />
            <el-option label="常规开发策略" value="normal" />
            <el-option label="快速扫描策略" value="fast" />
            <el-option label="CI/CD集成策略" value="cicd" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="policyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBindPolicy">确认绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Plus } from '@element-plus/icons-vue';

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(24);

const dialogVisible = ref(false);
const policyDialogVisible = ref(false);
const isEdit = ref(false);
const currentProject = ref<any>(null);

const projectForm = reactive({
  name: '',
  department: '',
  owner: '',
  gitUrl: '',
  policy: ''
});

const policyForm = reactive({
  policyId: ''
});

const projectList = ref([
  { name: '金融支付核心系统', department: '研发中心', owner: '张明', gitUrl: 'https://git.example.com/payment/core.git', policy: '高安全性策略', pluginCount: 28, critical: 5, high: 12, lastScan: '2024-01-15 14:32', score: 78 },
  { name: '用户中心服务', department: '研发中心', owner: '李华', gitUrl: 'https://git.example.com/user/center.git', policy: '常规开发策略', pluginCount: 35, critical: 3, high: 8, lastScan: '2024-01-15 13:20', score: 85 },
  { name: '交易网关系统', department: '研发中心', owner: '王芳', gitUrl: 'https://git.example.com/trade/gateway.git', policy: '快速扫描策略', pluginCount: 18, critical: 2, high: 6, lastScan: '2024-01-15 12:45', score: 82 },
  { name: '风控引擎服务', department: '安全中心', owner: '赵强', gitUrl: 'https://git.example.com/risk/engine.git', policy: '高安全性策略', pluginCount: 12, critical: 1, high: 4, lastScan: '2024-01-15 11:30', score: 91 },
  { name: '账户管理模块', department: '研发中心', owner: '钱伟', gitUrl: 'https://git.example.com/account/manage.git', policy: '常规开发策略', pluginCount: 22, critical: 1, high: 3, lastScan: '2024-01-15 10:15', score: 88 },
  { name: '订单处理系统', department: '业务中心', owner: '孙丽', gitUrl: 'https://git.example.com/order/process.git', policy: '常规开发策略', pluginCount: 15, critical: 0, high: 5, lastScan: '2024-01-15 09:00', score: 92 }
]);

function getScoreType(score: number) {
  if (score >= 90) return 'success';
  if (score >= 80) return 'warning';
  return 'danger';
}

function showAddDialog() {
  isEdit.value = false;
  Object.assign(projectForm, { name: '', department: '', owner: '', gitUrl: '', policy: '' });
  dialogVisible.value = true;
}

function editProject(row: any) {
  isEdit.value = true;
  Object.assign(projectForm, row);
  dialogVisible.value = true;
}

function bindPolicy(row: any) {
  currentProject.value = row;
  policyForm.policyId = row.policy;
  policyDialogVisible.value = true;
}

function deleteProject(row: any) {
  console.log('删除项目', row);
}

function saveProject() {
  console.log('保存项目', projectForm);
  dialogVisible.value = false;
}

function confirmBindPolicy() {
  console.log('绑定策略', policyForm);
  policyDialogVisible.value = false;
}
</script>

<style scoped>
.project-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; margin: 0; }

.stats-row { margin-bottom: 20px; }
.stat-mini { background: white; padding: 16px 20px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-num { font-size: 28px; font-weight: 700; color: var(--color-on-surface); }
.stat-label { font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px; }
.stat-mini.online .stat-num { color: #22c55e; }
.stat-mini.error .stat-num { color: #ef4444; }

.table-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }

.git-link { font-size: 12px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.risk-badge { display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 11px; margin-right: 4px; }
.risk-badge.critical { background: #FEE2E2; color: #DC2626; }
.risk-badge.high { background: #FFEDD5; color: #EA580C; }
</style>
