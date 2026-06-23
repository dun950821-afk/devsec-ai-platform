import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '../services/api';

export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone?: string;
  role: string;
  avatar?: string;
  org?: string;
}

interface LoginResponse {
  token: string;
  userInfo: UserInfo;
  expireTime?: number;
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const userInfo = ref<UserInfo | null>(null);

  const isLoggedIn = computed(() => !!token.value);

  async function login(username: string, password: string): Promise<boolean> {
    try {
      const result = await authApi.login(username, password) as LoginResponse;
      if (result.token) {
        token.value = result.token;
        userInfo.value = result.userInfo;
        localStorage.setItem('token', result.token);
        if (result.userInfo) {
          localStorage.setItem('userInfo', JSON.stringify(result.userInfo));
        }
        return true;
      }
      throw new Error('登录失败：未获取到有效令牌');
    } catch (error: any) {
      console.error('Login failed:', error);
      throw error; // 重新抛出错误以便页面显示
    }
  }

  async function logout() {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Logout failed:', error);
    }
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem('token');
  }

  // Initialize from stored token on page load
  function initFromStorage() {
    const storedToken = localStorage.getItem('token');
    const storedUserInfo = localStorage.getItem('userInfo');
    if (storedToken) {
      token.value = storedToken;
      if (storedUserInfo) {
        try {
          userInfo.value = JSON.parse(storedUserInfo);
        } catch (e) {
          console.error('Failed to parse stored user info', e);
        }
      }
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    initFromStorage
  };
});
