<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-header">
          <el-button type="primary" @click="openUserDialog()">新增用户</el-button>
        </div>
        <el-table :data="userList" stripe style="width: 100%">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="200" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openUserDialog(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="deleteUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 角色管理 -->
      <el-tab-pane label="角色管理" name="roles">
        <div class="role-list">
          <div v-for="role in roleList" :key="role.id" class="role-card">
            <div class="role-header">
              <span class="role-name">{{ role.name }}</span>
              <el-tag :type="role.status === 'active' ? 'success' : 'info'" size="small">
                {{ role.status === 'active' ? '启用' : '停用' }}
              </el-tag>
            </div>
            <p class="role-desc">{{ role.description }}</p>
          </div>
        </div>
      </el-tab-pane>

      <!-- 权限管理 -->
      <el-tab-pane label="权限管理" name="permissions">
        <el-tree :data="permissionTree" :props="{ label: 'name', children: 'children' }" default-expand-all />
      </el-tab-pane>

      <!-- 组织机构 -->
      <el-tab-pane label="组织机构" name="orgs">
        <el-tree :data="orgTree" :props="{ label: 'name', children: 'children' }" default-expand-all />
      </el-tab-pane>

      <!-- API Token -->
      <el-tab-pane label="API Token" name="tokens">
        <div class="tab-header">
          <el-button type="primary" @click="openTokenDialog()">创建Token</el-button>
        </div>
        <el-table :data="tokenList" stripe style="width: 100%">
          <el-table-column prop="name" label="Token名称" width="150" />
          <el-table-column prop="permissions" label="权限范围" min-width="200" />
          <el-table-column prop="expireTime" label="过期时间" width="140" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '正常' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="disableToken(row)">
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" size="small" @click="deleteToken(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 登录日志 -->
      <el-tab-pane label="登录日志" name="login-log">
        <el-table :data="loginLogList" stripe style="width: 100%">
          <el-table-column prop="time" label="登录时间" width="160" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="ip" label="IP地址" width="140" />
          <el-table-column prop="location" label="登录地点" width="160" />
          <el-table-column prop="browser" label="浏览器" min-width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                {{ row.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 用户对话框 -->
    <el-dialog v-model="userDialogVisible" title="用户信息" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="系统管理员" value="system_admin" />
            <el-option label="安全管理员" value="security_admin" />
            <el-option label="项目负责人" value="project_manager" />
            <el-option label="开发人员" value="developer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- Token对话框 -->
    <el-dialog v-model="tokenDialogVisible" title="创建Token" width="500px">
      <el-form :model="tokenForm" label-width="100px">
        <el-form-item label="Token名称">
          <el-input v-model="tokenForm.name" placeholder="请输入Token名称" />
        </el-form-item>
        <el-form-item label="权限范围">
          <el-checkbox-group v-model="tokenForm.permissions">
            <el-checkbox label="scan">扫描权限</el-checkbox>
            <el-checkbox label="result">结果查看</el-checkbox>
            <el-checkbox label="ai">AI调用</el-checkbox>
            <el-checkbox label="config">配置管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-select v-model="tokenForm.expireDays" placeholder="请选择过期时间">
            <el-option label="30天" value="30" />
            <el-option label="90天" value="90" />
            <el-option label="180天" value="180" />
            <el-option label="365天" value="365" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tokenDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createToken">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('users')
const userDialogVisible = ref(false)
const tokenDialogVisible = ref(false)
const isUserEdit = ref(false)

const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  role: ''
})

const tokenForm = reactive({
  name: '',
  permissions: [] as string[],
  expireDays: '30'
})

const userList = ref([
  { username: 'admin', email: 'admin@example.com', phone: '138****0001', role: '系统管理员', status: 'active', createTime: '2024-01-01 10:00' },
  { username: 'zhangsan', email: 'zhangsan@example.com', phone: '138****0002', role: '安全管理员', status: 'active', createTime: '2024-01-05 14:30' },
  { username: 'lisi', email: 'lisi@example.com', phone: '138****0003', role: '项目负责人', status: 'active', createTime: '2024-01-10 09:15' },
  { username: 'wangwu', email: 'wangwu@example.com', phone: '138****0004', role: '开发人员', status: 'inactive', createTime: '2024-02-01 11:20' }
])

const roleList = ref([
  { id: 1, name: '系统管理员', description: '拥有全部系统配置和管理权限', status: 'active' },
  { id: 2, name: '安全管理员', description: '管理项目、插件、策略、规则、检测结果、AI Skill', status: 'active' },
  { id: 3, name: '项目负责人', description: '查看本项目风险、插件接入情况和整改状态', status: 'active' },
  { id: 4, name: '开发人员', description: '使用插件扫描、查看个人项目风险、调用AI修复', status: 'active' },
  { id: 5, name: '审计人员', description: '查看插件审计日志、AI调用日志、数据上送日志', status: 'active' },
  { id: 6, name: '只读用户', description: '仅查看报表和风险数据', status: 'active' }
])

const permissionTree = ref([
  {
    name: '系统管理',
    children: [
      { name: '用户管理' },
      { name: '角色管理' },
      { name: '权限管理' },
      { name: '组织机构' }
    ]
  },
  {
    name: '项目管理',
    children: [
      { name: '项目列表' },
      { name: '项目详情' }
    ]
  },
  {
    name: '插件管理',
    children: [
      { name: '插件列表' },
      { name: '能力配置' }
    ]
  }
])

const orgTree = ref([
  {
    name: '国舜科技',
    children: [
      {
        name: '研发中心',
        children: [
          { name: '前端开发组' },
          { name: '后端开发组' },
          { name: '安全测试组' }
        ]
      },
      { name: '产品部' },
      { name: '市场部' }
    ]
  }
])

const tokenList = ref([
  { name: 'IDEA插件Token', permissions: '扫描权限、结果查看、AI调用', expireTime: '2026-12-31', status: 'active' },
  { name: 'CI/CD集成Token', permissions: '扫描权限、结果查看', expireTime: '2026-06-30', status: 'active' },
  { name: '测试环境Token', permissions: '结果查看', expireTime: '2026-03-15', status: 'inactive' }
])

const loginLogList = ref([
  { time: '2024-06-23 09:30:15', username: 'admin', ip: '192.168.1.100', location: '北京市海淀区', browser: 'Chrome 125.0', status: 'success' },
  { time: '2024-06-23 09:15:42', username: 'zhangsan', ip: '192.168.1.105', location: '北京市朝阳区', browser: 'Firefox 127.0', status: 'success' },
  { time: '2024-06-22 18:45:33', username: 'lisi', ip: '192.168.1.110', location: '北京市海淀区', browser: 'Safari 17.5', status: 'success' },
  { time: '2024-06-22 14:20:08', username: 'unknown', ip: '192.168.1.200', location: '未知', browser: '未知', status: 'failed' }
])

function openUserDialog(row?: any) {
  if (row) {
    isUserEdit.value = true
    Object.assign(userForm, row)
  } else {
    isUserEdit.value = false
    Object.assign(userForm, { username: '', email: '', phone: '', role: '' })
  }
  userDialogVisible.value = true
}

function saveUser() {
  ElMessage.success(isUserEdit.value ? '用户更新成功' : '用户创建成功')
  userDialogVisible.value = false
}

function deleteUser(row: any) {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userList.value = userList.value.filter(item => item.username !== row.username)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function openTokenDialog() {
  Object.assign(tokenForm, { name: '', permissions: [], expireDays: '30' })
  tokenDialogVisible.value = true
}

function createToken() {
  ElMessage.success('Token创建成功')
  tokenDialogVisible.value = false
}

function disableToken(row: any) {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  ElMessageBox.confirm(`确定要${newStatus === 'active' ? '启用' : '禁用'}该Token吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = newStatus
    ElMessage.success('操作成功')
  }).catch(() => {})
}

function deleteToken(row: any) {
  ElMessageBox.confirm('确定要删除该Token吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tokenList.value = tokenList.value.filter(item => item.name !== row.name)
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}
.settings-tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
}
.tab-header {
  margin-bottom: 16px;
}
.role-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.role-card {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}
.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.role-name {
  font-weight: 600;
  color: #303133;
}
.role-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
</style>
