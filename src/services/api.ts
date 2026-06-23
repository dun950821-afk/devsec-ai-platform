const API_BASE_URL = '/api';

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: any;
  headers?: Record<string, string>;
}

interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const result: ApiResponse<T> = await response.json();
  
  if (result.code !== 200) {
    throw new Error(result.message || 'Request failed');
  }

  return result.data;
}

// Auth API
export const authApi = {
  login: (username: string, password: string) =>
    request<{ token: string; userInfo: any }>('/auth/login', {
      method: 'POST',
      body: { username, password },
    }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};

// Dashboard API
export const dashboardApi = {
  getStats: () => request<any>('/dashboard'),
};

// Project API
export const projectApi = {
  list: (params: { current?: number; size?: number; keyword?: string }) =>
    request<any>('/project/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/project/${id}`),
  create: (data: any) => request('/project', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/project/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/project/${id}`, { method: 'DELETE' }),
};

// Plugin API
export const pluginApi = {
  list: (params: { current?: number; size?: number; keyword?: string; category?: string; status?: number }) =>
    request<any>('/plugin/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/plugin/${id}`),
  create: (data: any) => request('/plugin', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/plugin/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/plugin/${id}`, { method: 'DELETE' }),
  updateStatus: (id: string, status: number) =>
    request(`/plugin/${id}/status?status=${status}`, { method: 'PUT' }),
  getEnabled: () => request<any[]>('/plugin/enabled'),
  heartbeat: (pluginKey: string) =>
    request('/plugin/heartbeat?pluginKey=' + pluginKey, { method: 'POST' }),
  updateConfig: (id: string, config: any) =>
    request(`/plugin/${id}/config`, { method: 'PUT', body: config }),
};

// Policy API
export const policyApi = {
  list: (params: { current?: number; size?: number; keyword?: string; type?: string }) =>
    request<any>('/policy/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/policy/${id}`),
  create: (data: any) => request('/policy', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/policy/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/policy/${id}`, { method: 'DELETE' }),
  updateStatus: (id: string, status: number) =>
    request(`/policy/${id}/status?status=${status}`, { method: 'PUT' }),
};

// Rule API
export const ruleApi = {
  list: (params: { current?: number; size?: number; keyword?: string; severity?: string }) =>
    request<any>('/rule/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/rule/${id}`),
  create: (data: any) => request('/rule', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/rule/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/rule/${id}`, { method: 'DELETE' }),
  updateStatus: (id: string, status: number) =>
    request(`/rule/${id}/status?status=${status}`, { method: 'PUT' }),
};

// Result API
export const resultApi = {
  list: (params: { current?: number; size?: number; keyword?: string; severity?: string; status?: string }) =>
    request<any>('/result/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/result/${id}`),
  updateStatus: (id: string, status: string) =>
    request(`/result/${id}/status?status=${status}`, { method: 'PUT' }),
  getByProject: (projectId: string) =>
    request<any>(`/result/project/${projectId}`),
};

// Skill API
export const skillApi = {
  list: (params: { current?: number; size?: number; keyword?: string; type?: string }) =>
    request<any>('/skill/list', { method: 'GET', body: params }),
  getById: (id: string) => request<any>(`/skill/${id}`),
  create: (data: any) => request('/skill', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/skill/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/skill/${id}`, { method: 'DELETE' }),
  updateStatus: (id: string, status: number) =>
    request(`/skill/${id}/status?status=${status}`, { method: 'PUT' }),
};

// Audit Log API
export const auditApi = {
  list: (params: { current?: number; size?: number; keyword?: string; module?: string }) =>
    request<any>('/audit/list', { method: 'GET', body: params }),
};

// User API
export const userApi = {
  list: () => request<any[]>('/user/list'),
  getById: (id: string) => request<any>(`/user/${id}`),
  create: (data: any) => request('/user', { method: 'POST', body: data }),
  update: (id: string, data: any) => request(`/user/${id}`, { method: 'PUT', body: data }),
  delete: (id: string) => request(`/user/${id}`, { method: 'DELETE' }),
};
