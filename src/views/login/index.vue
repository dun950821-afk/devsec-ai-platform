<template>
  <div class="login-page">
    <h1>登录页面</h1>
    <form @submit.prevent="handleLogin">
      <input v-model="username" placeholder="用户名" />
      <input v-model="password" type="password" placeholder="密码" />
      <button type="submit">登录</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const username = ref('admin')
const password = ref('admin123')

const handleLogin = async () => {
  try {
    await userStore.login(username.value, password.value)
    router.push('/')
  } catch (error) {
    alert('登录失败')
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
</style>
