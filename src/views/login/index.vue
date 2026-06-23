<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-info">
        <div class="brand-logo">
          <el-icon class="shield-icon"><Lock /></el-icon>
          <h1 class="brand-name">国舜 DevSecAI</h1>
        </div>
        <p class="brand-desc">插件管理平台</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>SCA 开源组件检测</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>SAST 代码安全检测</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Secrets 敏感信息检测</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>AI 智能修复建议</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">欢迎使用国舜 DevSecAI 插件管理平台</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember">记住登录状态</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>

        <div class="login-footer">
          <span class="copyright">© 2024 北京国舜科技股份有限公司</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Check } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import type { FormInstance, FormRules } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: '',
  password: '',
  remember: false
});

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

async function handleLogin() {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const success = await userStore.login(form.username, form.password);
        if (success) {
          ElMessage.success('登录成功');
          router.push('/');
        } else {
          ElMessage.error('用户名或密码错误');
        }
      } finally {
        loading.value = false;
      }
    }
  });
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 50%, #60a5fa 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.brand-info {
  color: white;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.shield-icon {
  font-size: 64px;
  color: white;
}

.brand-name {
  font-size: 36px;
  font-weight: 700;
  margin: 0;
}

.brand-desc {
  font-size: 24px;
  opacity: 0.9;
  margin-bottom: 48px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 20px;
  background: rgba(255,255,255,0.2);
  padding: 6px;
  border-radius: 50%;
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 60px;
}

.login-card {
  width: 100%;
  max-width: 360px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: var(--color-on-surface-variant);
  margin: 0 0 40px 0;
}

.login-form {
  margin-bottom: 24px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid var(--color-outline);
}

.copyright {
  font-size: 12px;
  color: var(--color-on-surface-variant);
}
</style>
