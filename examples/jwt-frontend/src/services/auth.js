import axios from 'axios'

const API_BASE_URL = '/api'

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器 - 处理 Token 过期
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // 如果是 401 错误且不是重试请求
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      // 尝试刷新 Token（从 localStorage 获取）
      const token = localStorage.getItem('token')
      if (token) {
        try {
          const response = await api.post('/auth/refresh', { token })
          const newToken = response.data.data.token

          // 更新新 Token
          localStorage.setItem('token', newToken)
          originalRequest.headers.Authorization = `Bearer ${newToken}`

          // 重新发送原请求
          return axios(originalRequest)
        } catch (refreshError) {
          // 刷新失败，清除认证并重定向到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
        }
      }
    }

    return Promise.reject(error)
  }
)

/**
 * 获取认证头部
 */
const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export default {
  /**
   * 用户登录
   */
  async login(username, password) {
    return api.post('/auth/login', {
      username,
      password
    })
  },

  /**
   * 用户登出
   */
  async logout() {
    return api.post('/auth/logout', {}, {
      headers: getAuthHeader()
    })
  },

  /**
   * 刷新 Token
   */
  async refreshToken(token) {
    return api.post('/auth/refresh', { token })
  },

  /**
   * 获取当前用户信息
   */
  async getCurrentUser() {
    return api.get('/users/me', {
      headers: getAuthHeader()
    })
  },

  /**
   * 获取用户邮箱
   */
  async getUserEmail() {
    return api.get('/users/email', {
      headers: getAuthHeader()
    })
  },

  /**
   * 获取所有用户列表（需要管理员权限）
   */
  async getAllUsers() {
    return api.get('/admin/users', {
      headers: getAuthHeader()
    })
  },

  /**
   * 获取管理员欢迎信息
   */
  async getAdminWelcome() {
    return api.get('/admin/welcome', {
      headers: getAuthHeader()
    })
  },

  /**
   * 获取管理员统计信息
   */
  async getAdminStats() {
    return api.get('/admin/stats', {
      headers: getAuthHeader()
    })
  }
}