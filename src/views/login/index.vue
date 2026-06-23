<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand-section">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              <path d="M9 12l2 2 4-4"/>
            </svg>
          </div>
          <div class="logo-text">
            <h1 class="brand-name">国舜 DevSecAI</h1>
            <span class="brand-sub">智能安全检测平台</span>
          </div>
        </div>
        
        <p class="slogan">守护代码安全 · 赋能智能开发</p>
        
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon icon-sca">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <path d="M9 9h6M9 13h6M9 17h4"/>
              </svg>
            </span>
            <div class="feature-text">
              <span class="feature-title">SCA 开源组件检测</span>
              <span class="feature-desc">识别依赖漏洞与许可证风险</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-sast">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="16,18 22,12 16,6"/>
                <polyline points="8,6 2,12 8,18"/>
              </svg>
            </span>
            <div class="feature-text">
              <span class="feature-title">SAST 代码安全检测</span>
              <span class="feature-desc">深度扫描代码缺陷与安全漏洞</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-secrets">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </span>
            <div class="feature-text">
              <span class="feature-title">Secrets 敏感信息检测</span>
              <span class="feature-desc">自动发现硬编码凭证与密钥</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-ai">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z"/>
                <path d="M16 14h.01M8 14h.01M12 16v4M8 22h8"/>
              </svg>
            </span>
            <div class="feature-text">
              <span class="feature-title">AI 智能修复建议</span>
              <span class="feature-desc">大语言模型驱动的漏洞修复方案</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <div class="card-header">
          <h2 class="card-title">用户登录</h2>
          <p class="card-subtitle">欢迎使用国舜 DevSecAI 插件管理平台</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <label class="input-label">用户名</label>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="input-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-actions">
            <el-checkbox v-model="form.remember">记住登录状态</el-checkbox>
            <el-link type="primary" :underline="false" class="forgot-link">忘记密码？</el-link>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form>

        <div class="login-footer">
          <span>© 2024 北京国舜科技股份有限公司</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
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
    { required: true, message: '请输入密码', trigger: 'blur' }
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
        }
      } catch (error: any) {
        ElMessage.error(error.message || '用户名或密码错误');
      } finally {
        loading.value = false;
      }
    }
  });
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 50%, #3b82f6 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.brand-section {
  position: relative;
  z-index: 2;
  max-width: 520px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: rgba(255,255,255,0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-icon svg {
  width: 36px;
  height: 36px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 2px;
}

.brand-sub {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  margin-top: 4px;
}

.slogan {
  font-size: 20px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 48px;
  letter-spacing: 4px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: rgba(255,255,255,0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255,255,255,0.15);
  transform: translateX(8px);
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon svg {
  width: 22px;
  height: 22px;
}

.icon-sca {
  background: linear-gradient(135deg, #10b981, #059669);
}

.icon-sast {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.icon-secrets {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.icon-ai {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: white;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
}

.decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.05);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  bottom: 30%;
  right: 10%;
}

.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.card-header {
  margin-bottom: 40px;
}

.card-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

:deep(.custom-input) {
  --el-input-bg-color: #f9fafb;
  --el-input-border-color: #e5e7eb;
  --el-input-hover-border-color: #d1d5db;
  --el-input-focus-border-color: #2563eb;
  --el-input-border-radius: 10px;
  height: 48px;
}

:deep(.custom-input .el-input__wrapper) {
  background: #f9fafb;
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #e5e7eb;
  padding: 0 16px;
}

:deep(.custom-input .el-input__wrapper:hover) {
  border-color: #d1d5db;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-link {
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border: none;
  margin-top: 8px;
  transition: all 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

.login-footer {
  margin-top: 48px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
    min-height: 100vh;
  }
}
</style>
