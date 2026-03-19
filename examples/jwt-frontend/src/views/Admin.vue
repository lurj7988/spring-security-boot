<template>
  <div class="admin-container">
    <h1>管理员面板</h1>

    <div v-if="!hasAdminRole" class="error">
      <h2>访问被拒绝</h2>
      <p>您需要管理员权限才能访问此页面。</p>
      <router-link to="/home" class="back-link">返回首页</router-link>
    </div>

    <template v-else>
      <div v-if="loading" class="loading">
        正在加载...
      </div>

      <div v-else-if="error" class="error">
        {{ error }}
        <button @click="loadAdminData" class="retry-btn">重试</button>
      </div>

      <div v-else class="admin-content">
        <div class="welcome-card">
          <h2>欢迎，管理员！</h2>
          <p>您拥有所有权限，可以查看和管理系统数据。</p>
        </div>

        <div class="stats-card">
          <h2>用户统计</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.inactiveUsers }}</div>
              <div class="stat-label">非活跃用户</div>
            </div>
          </div>
        </div>

        <div class="users-card">
          <h2>所有用户</h2>
          <button @click="loadUsers" :disabled="loadingUsers" class="refresh-users-btn">
            {{ loadingUsers ? '加载中...' : '刷新用户列表' }}
          </button>

          <div v-if="users.length > 0" class="users-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>邮箱</th>
                  <th>状态</th>
                  <th>角色</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.id }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.email }}</td>
                  <td>
                    <span :class="['status-badge', user.status === 'active' ? 'active' : 'inactive']">
                      {{ user.status === 'active' ? '活跃' : '非活跃' }}
                    </span>
                  </td>
                  <td>
                    <div class="roles-cell">
                      <span
                        v-for="role in user.roles"
                        :key="role"
                        :class="['role-badge', role.toLowerCase().includes('admin') ? 'admin' : 'user']"
                      >
                        {{ role }}
                      </span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="!loadingUsers" class="no-users">
            暂无用户数据
          </div>
        </div>

        <div class="api-demo">
          <h2>管理员 API 演示</h2>
          <div class="api-buttons">
            <button @click="getAdminWelcome" :disabled="loadingWelcome" class="api-btn">
              {{ loadingWelcome ? '加载中...' : '获取欢迎信息' }}
            </button>
            <button @click="loadStats" :disabled="loadingStats" class="api-btn">
              {{ loadingStats ? '加载中...' : '刷新统计数据' }}
            </button>
            <button @click="loadUsers" :disabled="loadingUsers" class="api-btn">
              {{ loadingUsers ? '加载中...' : '获取用户列表' }}
            </button>
          </div>

          <div v-if="welcomeMessage" class="result success">
            <p>{{ welcomeMessage }}</p>
          </div>

          <div v-if="apiError" class="result error">
            <p>{{ apiError }}</p>
          </div>
        </div>

        <div class="demo-info">
          <h2>权限说明</h2>
          <div class="info-item">
            <h3>什么是管理员权限？</h3>
            <p>管理员角色（ROLE_ADMIN）拥有系统的所有权限，包括：</p>
            <ul>
              <li>查看所有用户信息</li>
              <li>访问管理员专用端点</li>
              <li>查看系统统计数据</li>
              <li>管理用户和权限（待实现）</li>
            </ul>
          </div>
          <div class="info-item">
            <h3>如何获得管理员权限？</h3>
            <p>在示例系统中，只有 admin 用户拥有管理员权限。</p>
          </div>
          <div class="info-item">
            <h3>权限是如何控制的？</h3>
            <p>后端使用 <code>@PreAuthorize("hasRole('ADMIN')")</code> 注解进行权限验证。</p>
            <p>前端通过检查用户角色来控制页面访问。</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import authAPI from '../services/auth'

const authStore = useAuthStore()

const loading = ref(false)
const loadingWelcome = ref(false)
const loadingStats = ref(false)
const loadingUsers = ref(false)
const error = ref('')
const apiError = ref('')
const welcomeMessage = ref('')
const stats = ref({
  totalUsers: 0,
  activeUsers: 0,
  inactiveUsers: 0
})
const users = ref([])

// 检查是否具有管理员权限
const hasAdminRole = computed(() => authStore.hasAdminRole)

// 获取管理员欢迎信息
const getAdminWelcome = async () => {
  loadingWelcome.value = true
  apiError.value = ''

  try {
    const response = await authAPI.getAdminWelcome()
    welcomeMessage.value = response.data.data.message
  } catch (err) {
    apiError.value = '获取欢迎信息失败'
    welcomeMessage.value = ''
  } finally {
    loadingWelcome.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  loadingStats.value = true
  apiError.value = ''

  try {
    const response = await authAPI.getAdminStats()
    stats.value = response.data.data
  } catch (err) {
    apiError.value = '获取统计数据失败'
  } finally {
    loadingStats.value = false
  }
}

// 加载用户列表
const loadUsers = async () => {
  loadingUsers.value = true
  apiError.value = ''

  try {
    const response = await authAPI.getAllUsers()
    users.value = response.data.data
  } catch (err) {
    apiError.value = '获取用户列表失败'
    users.value = []
  } finally {
    loadingUsers.value = false
  }
}

// 加载所有管理员数据
const loadAdminData = async () => {
  error.value = ''
  loading.value = true

  try {
    await Promise.all([
      getAdminWelcome(),
      loadStats(),
      loadUsers()
    ])
  } catch (err) {
    error.value = '加载数据失败'
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (hasAdminRole.value) {
    loadAdminData()
  }
})
</script>

<style scoped>
.admin-container {
  max-width: 1000px;
  margin: 0 auto;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
}

h2 {
  color: #333;
  margin-bottom: 15px;
}

.error {
  text-align: center;
  padding: 40px;
  background: #f8d7da;
  border-radius: 8px;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.back-link {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.back-link:hover {
  background: #0056b3;
}

.loading {
  text-align: center;
  font-size: 18px;
  color: #666;
  padding: 40px;
}

.retry-btn {
  margin-top: 20px;
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.admin-content {
  display: grid;
  gap: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
}

.stats-card, .users-card, .api-demo, .demo-info {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 16px;
}

.refresh-users-btn {
  margin-bottom: 20px;
  padding: 10px 20px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.refresh-users-btn:hover {
  background: #218838;
}

.refresh-users-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.users-table th, .users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.users-table th {
  background: #f8f9fa;
  font-weight: bold;
  color: #333;
}

.users-table tr:hover {
  background: #f5f5f5;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.status-badge.active {
  background: #28a745;
}

.status-badge.inactive {
  background: #6c757d;
}

.roles-cell {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.role-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.role-badge.admin {
  background: #dc3545;
}

.role-badge.user {
  background: #28a745;
}

.no-users {
  text-align: center;
  padding: 40px;
  color: #666;
}

.api-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
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

.demo-info .info-item {
  margin-bottom: 30px;
}

.demo-info h3 {
  color: #333;
  margin-top: 20px;
  margin-bottom: 10px;
}

.demo-info ul {
  margin-bottom: 15px;
  padding-left: 20px;
}

.demo-info li {
  margin-bottom: 8px;
  color: #666;
}

.demo-info code {
  background: #f8f9fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
  color: #dc3545;
}
</style>