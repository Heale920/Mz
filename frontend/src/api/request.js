import axios from 'axios'
import { ElMessage } from 'element-plus'
// import { useUserStore } from '@/stores/user'

const api = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000,
})

// 添加请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  console.log('当前 localStorage.token:', token); // 调试

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('请求头已注入 Token:', config.headers.Authorization); // 调试
  } else {
    console.warn('Token 不存在，请检查登录流程');
  }
  return config;
});

// 添加响应拦截器
api.interceptors.response.use(response => {
  const data = response.data
  if (!data || data.code !== "0") {
    ElMessage.error(data?.message || '请求失败')
    return Promise.reject(data)
  }
  return data
}, error => {
  if (error.response?.status === 401) {
    ElMessage.error('未授权，请重新登录')
    localStorage.removeItem('authToken')
    location.reload()
  }
  return Promise.reject(error)
})

// 接口封装
export const login = (formData) => {
  return api.post('/user/login', formData)
}

export const register = (userData) => {
  return api.post('/user/doRegister', userData)
}

export const logout = () => {
  localStorage.removeItem('authToken')
  localStorage.removeItem('userRole')
}

export default api
