import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  getters: {
    getAuthHeader() {
      return this.token ? { Authorization: `Bearer ${this.token}` } : {}
    },
    getUserRoles() {
      return this.user ? this.user.roles || [] : []
    },
    hasAdminRole() {
      return this.getUserRoles.includes('ROLE_ADMIN')
    }
  },

  actions: {
    /**
     * 登录
     */
    async login(username, password) {
      try {
        const response = await axios.post('/api/auth/login', {
          username,
          password
        })

        const { token, user } = response.data.data

        // 保存到 localStorage 和 store
        this.setToken(token)
        this.setUser(user)

        return { success: true, data: { token, user } }
      } catch (error) {
        return {
          success: false,
          error: error.response?.data?.message || '登录失败'
        }
      }
    },

    /**
     * 登出
     */
    async logout() {
      try {
        // 调用登出接口
        if (this.token) {
          await axios.post('/api/auth/logout', {}, {
            headers: this.getAuthHeader
          })
        }
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        // 清除数据
        this.clearAuth()
      }
    },

    /**
     * 刷新 Token
     */
    async refreshToken() {
      try {
        const response = await axios.post('/api/auth/refresh', {
          token: this.token
        })

        const { token } = response.data.data

        this.setToken(token)
        return { success: true, token }
      } catch (error) {
        // 刷新失败，清除认证状态
        this.clearAuth()
        return {
          success: false,
          error: 'Token 刷新失败'
        }
      }
    },

    /**
     * 设置 Token
     */
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)

      // 添加到 axios 默认配置
      axios.defaults.headers.common.Authorization = `Bearer ${token}`
    },

    /**
     * 设置用户信息
     */
    setUser(user) {
      this.user = user
      this.isAuthenticated = true
      localStorage.setItem('user', JSON.stringify(user))
    },

    /**
     * 清除认证信息
     */
    clearAuth() {
      this.token = null
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      // 移除 axios 默认配置
      delete axios.defaults.headers.common.Authorization
    },

    /**
     * 获取用户信息
     */
    async fetchUserInfo() {
      try {
        const response = await axios.get('/api/users/me', {
          headers: this.getAuthHeader
        })

        const user = response.data.data
        this.setUser(user)
        return { success: true, user }
      } catch (error) {
        if (error.response?.status === 401) {
          // Token 过期，尝试刷新
          const refreshResult = await this.refreshToken()
          if (refreshResult.success) {
            // 重新获取
            return this.fetchUserInfo()
          } else {
            this.clearAuth()
            return {
              success: false,
              error: '认证已过期，请重新登录'
            }
          }
        }
        return {
          success: false,
          error: error.response?.data?.message || '获取用户信息失败'
        }
      }
    }
  }
})