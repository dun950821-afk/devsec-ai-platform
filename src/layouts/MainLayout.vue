<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <path d="M16 2L4 8v8c0 7.18 5.12 13.89 12 15.42 6.88-1.53 12-8.24 12-15.42V8L16 2z" fill="#2563EB"/>
            <path d="M12 16l3 3 5-6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="logo-text">国舜 DevSecAI</span>
        </div>
      </div>
      <div class="header-center">
        <el-input placeholder="搜索项目、插件、规则..." prefix-icon="Search" clearable />
      </div>
      <div class="header-right">
        <div class="header-info">
          <span class="info-item">
            <el-tag size="small">当前项目</el-tag>
            全部项目
          </span>
          <span class="info-item">
            <el-tag size="small" type="success">策略版本</el-tag>
            v2.3.0
          </span>
          <span class="info-item">
            <el-tag size="small" type="info">规则包</el-tag>
            2026.06.23
          </span>
          <span class="info-item">
            <el-tag size="small" type="warning">在线插件</el-tag>
            168
          </span>
        </div>
        <el-badge :value="3" class="badge-item">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
        <el-dropdown trigger="click">
          <div class="user-info">
            <el-avatar :size="32" icon="UserFilled" />
            <span class="username">管理员</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item>修改密码</el-dropdown-item>
              <el-dropdown-item divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="main">
      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ collapsed: isCollapsed }">
        <el-menu
          :default-active="currentRoute"
          class="sidebar-menu"
          :collapse="isCollapsed"
          router
        >
          <el-menu-item index="/home">
            <el-icon><Odometer /></el-icon>
            <template #title>首页总览</template>
          </el-menu-item>
          <el-menu-item index="/project">
            <el-icon><FolderOpened /></el-icon>
            <template #title>项目管理</template>
          </el-menu-item>
          <el-menu-item index="/plugin">
            <el-icon><Connection /></el-icon>
            <template #title>插件管理</template>
          </el-menu-item>
          <el-menu-item index="/plugin-capability">
            <el-icon><Setting /></el-icon>
            <template #title>插件能力配置</template>
          </el-menu-item>
          <el-menu-item index="/policy">
            <el-icon><Document /></el-icon>
            <template #title>检测策略中心</template>
          </el-menu-item>
          <el-menu-item index="/rules">
            <el-icon><Collection /></el-icon>
            <template #title>规则管理中心</template>
          </el-menu-item>
          <el-menu-item index="/results">
            <el-icon><Warning /></el-icon>
            <template #title>检测结果中心</template>
          </el-menu-item>
          <el-menu-item index="/skill">
            <el-icon><Cpu /></el-icon>
            <template #title>大模型 Skill 中心</template>
          </el-menu-item>
          <el-menu-item index="/audit-log">
            <el-icon><Clock /></el-icon>
            <template #title>插件审计日志</template>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Tools /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
        <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
          <el-icon v-if="isCollapsed"><DArrowRight /></el-icon>
          <el-icon v-else><DArrowLeft /></el-icon>
        </div>
      </aside>

      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Odometer, FolderOpened, Connection, Setting,
  Document, Collection, Warning, Cpu, Clock, Tools,
  Bell, UserFilled, ArrowDown, DArrowLeft, DArrowRight
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapsed = ref(false)
const currentRoute = computed(() => route.path)
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1e40af;
}

.header-center {
  flex: 1;
  max-width: 480px;
  margin: 0 48px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.badge-item {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

.main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  position: relative;
  width: 220px;
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-menu {
  height: calc(100vh - 60px - 40px);
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.sidebar-toggle {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-top: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background 0.2s;
}

.sidebar-toggle:hover {
  background: #f0f0f0;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
