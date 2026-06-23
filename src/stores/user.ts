import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface UserInfo {
  id: string;
  username: string;
  name: string;
  email: string;
  phone?: string;
  role: string;
  avatar?: string;
  org?: string;
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const userInfo = ref<UserInfo | null>(null);

  const isLoggedIn = computed(() => !!token.value);

  function login(username: string, password: string): Promise<boolean> {
    return new Promise((resolve) => {
      // Mock 登录
      setTimeout(() => {
        if (username === 'admin' && password === 'admin123') {
          token.value = 'mock-token-' + Date.now();
          userInfo.value = {
            id: '1',
            username: 'admin',
            name: '系统管理员',
            email: 'admin@guoshun.com',
            phone: '138****8888',
            role: '系统管理员',
            org: '国舜科技'
          };
          localStorage.setItem('token', token.value!);
          resolve(true);
        } else {
          resolve(false);
        }
      }, 500);
    });
  }

  function logout() {
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem('token');
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout
  };
});
