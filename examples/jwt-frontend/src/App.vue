<template>
  <div id="app">
    <div class="container">
      <!-- 导航栏 -->
      <nav class="nav" v-if="isAuthenticated">
        <button
          :class="['nav-button', { active: $route.path === '/home' }]"
          @click="$router.push('/home')"
        >
          首页
        </button>
        <button
          :class="['nav-button', { active: $route.path === '/admin' }]"
          @click="$router.push('/admin')"
        >
          管理员
        </button>
        <button class="logout-btn" @click="logout">
          登出
        </button>
      </nav>

      <!-- 显示当前视图 -->
      <div class="content">
        <!-- 登录页面 -->
        <div v-if="$route.path === '/login'" class="view active">
          <Login />
        </div>

        <!-- 首页 -->
        <div v-if="$route.path === '/home'" class="view active">
          <Home />
        </div>

        <!-- 管理页面 -->
        <div v-if="$route.path === '/admin'" class="view active">
          <Admin />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from './stores/auth'
import Login from './views/Login.vue'
import Home from './views/Home.vue'
import Admin from './views/Admin.vue'

const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)

const logout = () => {
  authStore.logout()
  // 重定向到登录页
  window.location.href = '/login'
}
</script>

<style>
/* 基础样式已在 index.html 中定义 */
.content {
  min-height: 400px;
}

.nav-button {
  padding: 8px 16px;
  border: none;
  background: #007bff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.nav-button:hover {
  background: #0056b3;
}

.nav-button.active {
  background: #28a745;
}

.logout-btn {
  margin-left: auto;
  background: #dc3545;
}

.logout-btn:hover {
  background: #c82333;
}
</style>