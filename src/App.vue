<template>
  <div id="app">
    <div v-if="!isLoggedIn">
      <div class="login-container">
        <div class="login-left">
          <div class="brand">
            <div class="logo">DS</div>
            <h1>DevSecAI</h1>
          </div>
          <p class="slogan">智能安全开发平台</p>
          <div class="features">
            <div class="feature-item">SCA 组件分析</div>
            <div class="feature-item">SAST 代码扫描</div>
            <div class="feature-item">Secrets 密钥检测</div>
            <div class="feature-item">AI 智能修复</div>
          </div>
        </div>
        <div class="login-right">
          <h2>用户登录</h2>
          <p class="subtitle">欢迎使用 DevSecAI 智能安全开发平台</p>
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label>用户名</label>
              <input v-model="username" type="text" placeholder="请输入用户名" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input v-model="password" type="password" placeholder="请输入密码" />
            </div>
            <div class="error" v-if="errorMessage">{{ errorMessage }}</div>
            <div class="success" v-if="successMessage">{{ successMessage }}</div>
            <button type="submit" :disabled="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </button>
          </form>
          <div class="footer">
            <p>DevSecAI v1.0.0</p>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="dashboard">
      <header class="header">
        <div class="header-left">
          <span class="logo-small">DS</span>
          <span class="title">DevSecAI</span>
        </div>
        <div class="header-right">
          <span>管理员</span>
          <button @click="handleLogout">退出</button>
        </div>
      </header>
      <div class="content">
        <h2>欢迎使用 DevSecAI 智能安全开发平台</h2>
        <p>首页加载成功！</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const token = ref(localStorage.getItem('token') || '')

const isLoggedIn = computed(() => !!token.value)

const handleLogin = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  
  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  
  loading.value = true
  
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      token.value = data.data.token
      localStorage.setItem('token', data.data.token)
      successMessage.value = '登录成功！正在跳转...'
      setTimeout(() => {
        successMessage.value = ''
      }, 2000)
    } else {
      errorMessage.value = data.message || '登录失败'
    }
  } catch (error) {
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  token.value = ''
  localStorage.removeItem('token')
  username.value = 'admin'
  password.value = ''
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
}

.login-container {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.logo {
  width: 60px;
  height: 60px;
  background: white;
  color: #667eea;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.brand h1 {
  font-size: 36px;
  font-weight: 600;
}

.slogan {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.feature-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
}

.login-right {
  width: 480px;
  background: white;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-right h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  margin-bottom: 40px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.error {
  color: #e74c3c;
  padding: 10px;
  background: #fdeaea;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
}

.success {
  color: #27ae60;
  padding: 10px;
  background: #eafaf1;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
}

button[type="submit"] {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

button[type="submit"]:hover:not(:disabled) {
  transform: translateY(-2px);
}

button[type="submit"]:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.footer {
  margin-top: 40px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-small {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right button {
  padding: 8px 16px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.content {
  padding: 24px;
}
</style>
