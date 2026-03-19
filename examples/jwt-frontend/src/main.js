import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import App from './App.vue'

// 路由配置
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('./views/Login.vue') },
  { path: '/home', component: () => import('./views/Home.vue') },
  { path: '/admin', component: () => import('./views/Admin.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 如果访问需要认证的页面但没有 token，重定向到登录页
  if ((to.path === '/home' || to.path === '/admin') && !token) {
    next('/login')
    return
  }

  // 如果已经登录且访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  // 如果访问管理页面，检查权限
  if (to.path === '/admin') {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      const hasAdminRole = user.roles && user.roles.includes('ROLE_ADMIN')
      if (!hasAdminRole) {
        // 不具有管理员权限，重定向到首页
        next('/home')
        return
      }
    }
  }

  next()
})

// Pinia 状态管理
const pinia = createPinia()

// 创建应用
const app = createApp(App)

app.use(pinia)
app.use(router)

app.mount('#app')