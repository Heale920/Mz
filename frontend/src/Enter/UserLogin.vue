<template>
  <div class="login-page">
    <!-- 左侧品牌展示 -->
    <div class="left-section">
      <div class="logo-container">
        <!-- 采用了更具现代感的字体方案 -->
        <h1 class="brand-title">码志</h1>
        <p class="slogan">写给程序员的代码笔记</p>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="right-section">
      <!-- 光效已添加动画 -->
      <div class="light-effect"></div>
      <div class="login-card">
        <div class="login-header">
          <h1>欢迎 <span class="highlight">登录</span></h1>
          <p class="subtitle">开启你的技术之旅</p>
        </div>

        <el-form
            :model="formModel"
            :rules="rules"
            ref="form"
            size="large"
            autocomplete="off"
            class="login-form"
        >
          <!-- 添加了交错动画效果 -->
          <transition name="fade-up" appear>
            <el-form-item prop="username" style="--stagger-delay: 0.1s">
              <el-input
                  v-model="formModel.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
              />
            </el-form-item>
          </transition>

          <transition name="fade-up" appear>
            <el-form-item prop="password" style="--stagger-delay: 0.2s">
              <el-input
                  v-model="formModel.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  :prefix-icon="Lock"
              />
            </el-form-item>
          </transition>

          <transition name="fade-up" appear>
            <el-form-item style="--stagger-delay: 0.3s">
              <el-button
                  type="primary"
                  @click="onSubmit"
                  class="login-btn"
                  :loading="isLoading"
              >
                {{ isLoading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </transition>

          <div class="login-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/request'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// --- UX 优化：增加加载状态 ---
const isLoading = ref(false)

const formModel = ref({
  username: '',
  password: '',
})

const form = ref()

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 10, message: '用户名必须是 3-10位 的字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 15, message: '密码必须是 6-15位 的字符', trigger: 'blur' },
  ],
}

const reset = () => {
  formModel.value.username = ''
  formModel.value.password = ''
}

const onSubmit = async () => {
  try {
    await form.value.validate()
    isLoading.value = true // --- 开始加载 ---

    const res = await login(formModel.value)

    if (!res.data?.token || !res.data?.user) {
      throw new Error('返回数据缺少必要字段')
    }

    userStore.loginSuccess({
      id: res.data.user.id,
      username: res.data.user.username,
      avatarUrl: res.data.user.avatar,
      token: res.data.token,
      role: res.data.user.role,
    })

    ElMessage.success('登录成功！')
    const role = res.data.user.role
    router.push(role === 'admin' ? '/dashboard' : '/dashboard1')

    reset()
  } catch (error) {
    if (error?.code === '0') return
    ElMessage.error(error.message || '登录失败')
  } finally {
    isLoading.value = false // --- 无论成功失败，结束加载 ---
  }
}
</script>

<style scoped>
/* 定义全局变量 */
:root {
  --bg-dark-primary: #0a192f;
  --bg-dark-secondary: #0d2542;
  --text-primary: #e6f1ff;
  --text-secondary: #ccd6f6;
  --text-muted: #8892b0;
  --accent-color: #64ffda;
  --border-radius-card: 16px;
  --border-radius-input: 8px;
}

.login-page {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, var(--bg-dark-primary), var(--bg-dark-secondary));
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, sans-serif; /* 使用更现代的西文字体 */
  overflow: hidden;
  position: relative;
}

/* 左侧 */
.left-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.logo-container {
  text-align: center;
}

.brand-title {
  /* 使用 clamp 实现响应式字体，避免在大屏上过大 */
  font-size: clamp(4rem, 12vw, 10rem);
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.1em;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
  /* 优化了动画效果 */
  animation: slide-in-bottom 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.slogan {
  margin-top: 10px;
  font-size: clamp(1.5rem, 4vw, 2.5rem);
  color: var(--text-secondary);
  opacity: 0;
  animation: fade-in 0.7s 0.5s ease-in forwards;
}


/* 右侧 */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 光效动画 */
@keyframes rotate-glow {
  0% {
    transform: rotate(0deg) scale(1.5);
  }
  100% {
    transform: rotate(360deg) scale(1.5);
  }
}

.light-effect {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(100, 255, 218, 0.1), transparent 40%);
  animation: rotate-glow 25s linear infinite; /* 添加缓慢旋转动画 */
  pointer-events: none;
  z-index: 0;
}

.login-card {
  width: 380px;
  padding: 40px;
  background: rgba(13, 37, 66, 0.3);
  border-radius: var(--border-radius-card);
  border: 1px solid rgba(100, 255, 218, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(15px);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.highlight {
  color: var(--accent-color);
}

.subtitle {
  font-size: 1rem;
  color: var(--text-muted);
  margin-top: 8px;
}


/* 表单 - 使用 CSS 变量进行深度定制 */
.login-form {
  --el-color-primary: var(--accent-color);
  --el-border-radius-base: var(--border-radius-input);
}

.login-form .el-form-item {
  /* --- 这是解决您问题的核心 --- */
  --el-input-text-color: var(--text-primary); /* 输入文字颜色 */
  --el-input-placeholder-color: var(--text-muted); /* 占位符颜色 */
  --el-input-bg-color: rgba(13, 37, 66, 0.5); /* 输入框背景 */
  --el-input-border-color: rgba(100, 255, 218, 0.2);
  --el-input-hover-border-color: var(--accent-color);
  --el-input-focus-border-color: var(--accent-color);
}

.login-form .el-input__prefix {
  color: var(--accent-color) !important;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 1.1rem;
  font-weight: bold;
  background: var(--accent-color);
  color: var(--bg-dark-primary);
  border: none;
  transition: all 0.3s ease;
}
.login-btn:hover {
  background: var(--accent-color);
  color: var(--bg-dark-primary);
  box-shadow: 0 0 15px rgba(100, 255, 218, 0.4);
}


/* 底部注册 */
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.register-link {
  color: var(--accent-color);
  font-weight: bold;
  margin-left: 5px;
  text-decoration: none;
}
.register-link:hover {
  text-decoration: underline;
}

/* 动画效果优化 */
@keyframes slide-in-bottom {
  0% { transform: translateY(50px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}
@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.fade-up-enter-active {
  animation: slide-in-bottom 0.5s var(--stagger-delay, 0s) cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
</style>