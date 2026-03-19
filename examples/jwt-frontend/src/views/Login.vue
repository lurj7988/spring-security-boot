<template>
  <div class="login-container">
    <h1>用户登录</h1>
    <form @submit.prevent="handleLogin" class="login-form">
      <div class="form-group">
        <label for="username">用户名</label>
        <input
          type="text"
          id="username"
          v-model="formData.username"
          required
          placeholder="请输入用户名"
        />
      </div>

      <div class="form-group">
        <label for="password">密码</label>
        <input
          type="password"
          id="password"
          v-model="formData.password"
          required
          placeholder="请输入密码"
        />
      </div>

      <button type="submit" :disabled="loading" class="submit-btn">
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <div v-if="error" class="error">
        {{ error }}
      </div>

      <div class="test-accounts">
        <h3>测试账户</h3>
        <p class="warning">以下为示例测试账户，密码请联系管理员获取</p>
        <ul>
          <li>用户名: <code>admin</code> (管理员角色)</li>
          <li>用户名: <code>user</code> (普通用户角色)</li>
          <li>用户名: <code>test</code> (普通用户角色)</li>
        </ul>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import authAPI from '../services/auth'

const authStore = useAuthStore()

const formData = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  // 重置错误
  error.value = ''
  loading.value = true

  try {
    const result = await authStore.login(
      formData.value.username,
      formData.value.password
    )

    if (result.success) {
      // 登录成功，获取用户信息
      await authStore.fetchUserInfo()

      // 重定向到首页
      window.location.href = '/home'
    } else {
      error.value = result.error || '登录失败'
    }
  } catch (err) {
    error.value = '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 路由变化时清除错误（通过 watchEffect 实现）
// 注意：原代码尝试访问 window.router，该对象不存在，已移除
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  text-align: center;
}

.login-form {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.submit-btn:hover {
  background: #0056b3;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error {
  color: #dc3545;
  margin: 15px 0;
  padding: 10px;
  background: #f8d7da;
  border-radius: 4px;
  border: 1px solid #f5c6cb;
}

.test-accounts {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: left;
}

.test-accounts h3 {
  margin-top: 0;
  color: #333;
}

.test-accounts ul {
  margin-bottom: 0;
}

.test-accounts li {
  margin-bottom: 8px;
  color: #666;
}
</style>