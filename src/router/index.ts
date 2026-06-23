import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/stores/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页总览' }
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/views/project/index.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'plugin',
        name: 'Plugin',
        component: () => import('@/views/plugin/index.vue'),
        meta: { title: '插件管理' }
      },
      {
        path: 'plugin-capability',
        name: 'PluginCapability',
        component: () => import('@/views/plugin-capability/index.vue'),
        meta: { title: '插件能力配置' }
      },
      {
        path: 'policy',
        name: 'Policy',
        component: () => import('@/views/policy/index.vue'),
        meta: { title: '检测策略中心' }
      },
      {
        path: 'rules',
        name: 'Rules',
        component: () => import('@/views/rules/index.vue'),
        meta: { title: '规则管理中心' }
      },
      {
        path: 'results',
        name: 'Results',
        component: () => import('@/views/results/index.vue'),
        meta: { title: '检测结果中心' }
      },
      {
        path: 'skill',
        name: 'Skill',
        component: () => import('@/views/skill/index.vue'),
        meta: { title: '大模型Skill中心' }
      },
      {
        path: 'audit-log',
        name: 'AuditLog',
        component: () => import('@/views/audit-log/index.vue'),
        meta: { title: '插件审计日志' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  
  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next('/login');
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/');
  } else {
    next();
  }
});

export default router;
