<template>
  <div class="login-wrapper">
    <div class="login-left">
      <div class="brand-content">
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              <path d="M9 12l2 2 4-4"/>
            </svg>
          </div>
          <div class="logo-text">
            <h1>国舜 DevSecAI</h1>
            <span>智能安全检测平台</span>
          </div>
        </div>
        
        <p class="slogan">守护代码安全 · 赋能智能开发</p>
        
        <div class="features-list">
          <div class="feature-item">
            <span class="feature-icon icon-sca">📦</span>
            <div>
              <span class="feature-title">SCA 开源组件检测</span>
              <span class="feature-desc">识别依赖漏洞与许可证风险</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-sast">🔍</span>
            <div>
              <span class="feature-title">SAST 代码安全检测</span>
              <span class="feature-desc">深度扫描代码缺陷与安全漏洞</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-secrets">🔑</span>
            <div>
              <span class="feature-title">Secrets 敏感信息检测</span>
              <span class="feature-desc">自动发现硬编码凭证与密钥</span>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon icon-ai">🤖</span>
            <div>
              <span class="feature-title">AI 智能修复建议</span>
              <span class="feature-desc">大语言模型驱动的漏洞修复方案</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">欢迎使用国舜 DevSecAI 插件管理平台</p>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <div class="form-group">
            <label>用户名</label>
            <input 
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input 
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="form.remember" />
              <span>记住登录状态</span>
            </label>
            <a href="#" class="forgot-pwd">忘记密码？</a>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <div class="login-copyright">
          © 2024 北京国舜科技股份有限公司
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const errorMessage = ref('');

const form = reactive({
  username: '',
  password: '',
  remember: false
});

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  
  loading.value = true;
  errorMessage.value = '';
  
  try {
    await userStore.login(form.username, form.password);
    ElMessage.success('登录成功，正在跳转...');
    setTimeout(() => {
      router.push('/');
    }, 500);
  } catch (error: any) {
    console.error('Login error:', error);
    errorMessage.value = error.message || '用户名或密码错误';
    ElMessage.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 50%, #3b82f6 100%);
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: rgba(255,255,255,0.03);
  border-radius: 50%;
  top: -150px;
  right: -150px;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255,255,255,0.03);
  border-radius: 50%;
  bottom: -100px;
  left: -100px;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 520px;
  width: 100%;
}

.logo-area {
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

.logo-text h1 {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 2px;
}

.logo-text span {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  margin-top: 4px;
  display: block;
}

.slogan {
  font-size: 20px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 48px;
  letter-spacing: 4px;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
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
  font-size: 20px;
  flex-shrink: 0;
}

.icon-sca { background: linear-gradient(135deg, #10b981, #059669); }
.icon-sast { background: linear-gradient(135deg, #f59e0b, #d97706); }
.icon-secrets { background: linear-gradient(135deg, #ef4444, #dc2626); }
.icon-ai { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.feature-item > div {
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

.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-box {
  width: 100%;
  max-width: 380px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 40px 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.error-message {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
  text-align: center;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-group input {
  height: 48px;
  padding: 0 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.form-group input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  background: white;
}

.form-group input::placeholder {
  color: #9ca3af;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.forgot-pwd {
  color: #2563eb;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-pwd:hover {
  color: #1d4ed8;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-copyright {
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
