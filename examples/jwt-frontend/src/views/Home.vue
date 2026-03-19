<template>
  <div class="home-container">
    <h1>欢迎回来！</h1>

    <div v-if="loading" class="loading">
      正在加载...
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="fetchUserInfo" class="retry-btn">重试</button>
    </div>

    <div v-else class="user-info">
      <div class="info-card">
        <h2>基本信息</h2>
        <p><strong>用户名：</strong>{{ user?.username || '未登录' }}</p>
        <p><strong>邮箱：</strong>{{ user?.email || '未提供' }}</p>
        <p><strong>手机号：</strong>{{ user?.phone || '未提供' }}</p>
      </div>

      <div class="info-card">
        <h2>权限信息</h2>
        <div class="roles">
          <span
            v-for="role in user?.roles || []"
            :key="role"
            :class="['role-badge', role.toLowerCase().includes('admin') ? 'admin' : 'user']"
          >
            {{ role }}
          </span>
          <div v-if="!user?.roles || user.roles.length === 0">
            暂无权限
          </div>
        </div>
      </div>

      <div class="info-card">
        <h2>演示说明</h2>
        <div class="demo-info">
          <p>• 您已成功登录系统</p>
          <p>• 这是一个需要认证的页面</p>
          <p>• 您的 JWT Token 存储在浏览器中</p>
          <p>• 如果您是管理员，可以访问<strong><a href="/admin" class="admin-link">管理员页面</a></strong></p>
          <p>• <a href="#" @click="handleLogout" class="logout-link">退出登录</a></p>
        </div>
      </div>

      <div class="api-demo">
        <h2>API 调用演示</h2>
        <div class="api-buttons">
          <button @click="getUserEmail" :disabled="loadingEmail" class="api-btn">
            {{ loadingEmail ? '获取中...' : '获取我的邮箱' }}
          </button>
          <button @click="refreshToken" :disabled="refreshing" class="api-btn refresh-btn">
            {{ refreshing ? '刷新中...' : '刷新 Token' }}
          </button>
        </div>

        <div v-if="email" class="result success">
          <p><strong>邮箱：</strong>{{ email }}</p>
        </div>

        <div v-if="refreshMessage" :class="['result', refreshSuccess ? 'success' : 'error']">
          <p>{{ refreshMessage }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import authAPI from '../services/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const loading = ref(true)
const loadingEmail = ref(false)
const refreshing = ref(false)
const error = ref('')
const email = ref('')
const refreshMessage = ref('')
const refreshSuccess = ref(false)

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  error.value = ''

  try {
    await authStore.fetchUserInfo()
  } catch (err) {
    error.value = '获取用户信息失败'
  } finally {
    loading.value = false
  }
}

// 获取用户邮箱
const getUserEmail = async () => {
  loadingEmail.value = true

  try {
    const response = await authAPI.getUserEmail()
    email.value = response.data.data
    refreshMessage.value = ''
  } catch (err) {
    email.value = ''
    refreshMessage.value = '获取邮箱失败'
    refreshSuccess.value = false
  } finally {
    loadingEmail.value = false
  }
}

// 刷新 Token
const refreshToken = async () => {
  refreshing.value = true
  refreshMessage.value = ''
  refreshSuccess.value = false

  try {
    const response = await authAPI.refreshToken(
      localStorage.getItem('token')
    )

    const newToken = response.data.data.token
    localStorage.setItem('token', newToken)

    refreshMessage.value = 'Token 刷新成功'
    refreshSuccess.value = true
  } catch (err) {
    refreshMessage.value = 'Token 刷新失败，请重新登录'
    refreshSuccess.value = false
  } finally {
    refreshing.value = false
  }
}

// 处理登出
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    authStore.logout()
    window.location.href = '/login'
  }
}

// 组件挂载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>


<style scoped>
.home-container {
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
}

.loading {
  text-align: center;
  font-size: 18px;
  color: #666;
  padding: 40px;
}

.error {
  color: #dc3545;
  padding: 20px;
  background: #f8d7da;
  border-radius: 4px;
  border: 1px solid #f5c6cb;
  text-align: center;
}

.retry-btn {
  margin-top: 10px;
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.user-info {
  display: grid;
  gap: 20px;
}

.info-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.info-card h2 {
  color: #333;
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
}

.info-card p {
  margin: 10px 0;
  color: #666;
}

.roles {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.role-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  color: white;
}

.role-badge.admin {
  background: #dc3545;
}

.role-badge.user {
  background: #28a745;
}

.demo-info {
  color: #666;
  line-height: 1.8;
}

.demo-info p {
  margin: 8px 0;
}

.admin-link {
  color: #dc3545;
  text-decoration: none;
  font-weight: bold;
}

.admin-link:hover {
  text-decoration: underline;
}

.logout-link {
  color: #dc3545;
  text-decoration: none;
  cursor: pointer;
}

.logout-link:hover {
  text-decoration: underline;
}

.api-demo {
  text-align: center;
}

.api-buttons {
  margin: 20px 0;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.api-btn {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.api-btn:hover {
  background: #0056b3;
}

.api-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.refresh-btn {
  background: #28a745;
}

.refresh-btn:hover {
  background: #218838;
}

.result {
  padding: 15px;
  border-radius: 4px;
  margin-top: 15px;
}

.result.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.result.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>