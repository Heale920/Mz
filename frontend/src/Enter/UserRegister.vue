<template>
  <div class="register-page">
    <!-- 左侧注册表单 -->
    <div class="left-section">
      <div class="light-effect"></div>
      <div class="register-card">
        <div class="register-header">
          <h1>创建您的<span class="highlight">账户</span></h1>
          <p class="subtitle">加入我们，即刻启航</p>
        </div>

        <el-form
            :model="formModel"
            :rules="rules"
            ref="form"
            size="large"
            autocomplete="off"
            class="register-form"
            label-position="top"
        >
          <!-- 关键改动：使用 label 属性，而不是将文字写在外面 -->
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="formModel.username"
                placeholder="创建您的专属用户名"
                :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="邮箱地址" prop="email">
            <el-input
                v-model="formModel.email"
                placeholder="用于接收重要通知"
                :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
                v-model="formModel.password"
                type="password"
                placeholder="至少6位，包含字母和数字"
                show-password
                :prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 最佳实践：添加确认密码字段 -->
          <el-form-item label="确认密码" prop="repassword">
            <el-input
                v-model="formModel.repassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                @click="onSubmit"
                class="register-btn"
                :loading="isLoading"
            >
              {{ isLoading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>

          <div class="register-footer">
            <span>已有账户？</span>
            <router-link to="/login" class="login-link">直接登录</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 右侧品牌展示 (与登录页镜像) -->
    <div class="right-section">
      <div class="logo-container">
        <h1 class="brand-title">码志</h1>
        <p class="slogan">写给程序员的代码笔记</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { register } from '@/api/request';
import { User, Lock, Message } from '@element-plus/icons-vue'; // 引入 Message 图标

const router = useRouter();
const isLoading = ref(false);

const formModel = ref({
  username: '',
  email: '',
  password: '',
  repassword: '', // 新增确认密码字段
});

const form = ref();

// 确认密码的自定义校验规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== formModel.value.password) {
    callback(new Error('两次输入的密码不一致!'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 10, message: '用户名必须是3-10位的字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]{6,15}$/, message: '密码必须是6-15位的字母或数字', trigger: 'blur' },
  ],
  repassword: [
    { validator: validatePass, trigger: 'blur' }, // 使用自定义校验器
  ],
};

const reset = () => {
  form.value.resetFields();
};

const onSubmit = async () => {
  try {
    await form.value.validate();
    isLoading.value = true;

    // 提交给后端的模型不应包含 repassword
    const { username, password, email } = formModel.value;
    const res = await register({ username, password, email });

    // 这里假设 code 为 '0' 表示成功
    if (res.code === "0") {
      ElMessage.success('注册成功！即将跳转到登录页...');
      setTimeout(() => {
        router.push('/login');
      }, 1500);
      reset();
    } else {
      // 从后端获取具体的错误信息
      throw new Error(res.message || '注册失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请检查网络或服务器状态');
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 完全复用并适应登录页的样式，但类名更具语义化 */
:root {
  --bg-dark-primary: #111827;
  --bg-dark-secondary: #0c111d;
  --text-primary: #f9fafb;
  --text-secondary: #e5e7eb;
  --text-muted: #9ca3af;
  --accent-color-primary: #818cf8;
  --accent-color-hover: #6366f1;
  --accent-color-primary-rgb: 129, 140, 248;
  --border-radius-card: 16px;
  --border-radius-input: 8px;
}

.register-page {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, var(--bg-dark-primary), var(--bg-dark-secondary));
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, sans-serif;
  overflow: hidden;
  position: relative;
}

/* 左侧表单区域 */
.left-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 右侧品牌区域 (镜像) */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.brand-title, .slogan { /* 动画效果复用 */
  animation: slide-in-bottom 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
.slogan { animation-delay: 0.2s; }
.brand-title { font-size: clamp(4rem, 12vw, 10rem); font-weight: 800; color: #fff; letter-spacing: 0.1em; }
.slogan { margin-top: 10px; font-size: clamp(1.5rem, 4vw, 2.5rem); color: var(--text-secondary); }


@keyframes rotate-glow {
  0% { transform: rotate(0deg) scale(1.5); }
  100% { transform: rotate(360deg) scale(1.5); }
}

.light-effect {
  position: absolute;
  top: -50%;
  left: -50%; /* 光效也镜像到左上角 */
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(var(--accent-color-primary-rgb), 0.1), transparent 40%);
  animation: rotate-glow 25s linear infinite;
  pointer-events: none;
  z-index: 0;
}

.register-card {
  width: 400px;
  padding: 40px;
  background: rgba(23, 26, 35, 0.4);
  border-radius: var(--border-radius-card);
  border: 1px solid rgba(var(--accent-color-primary-rgb), 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(15px);
  position: relative;
  z-index: 1;
}

.register-header { text-align: center; margin-bottom: 24px; }
.register-header h1 { font-size: 2rem; font-weight: 700; color: var(--text-primary); }
.highlight { color: var(--accent-color-primary); }
.subtitle { font-size: 1rem; color: var(--text-muted); margin-top: 8px; }

.register-form {
  --el-color-primary: var(--accent-color-primary);
  --el-border-radius-base: var(--border-radius-input);
  --el-form-label-color: var(--text-secondary);
}

.register-form .el-form-item {
  --el-input-text-color: var(--text-primary);
  --el-input-placeholder-color: var(--text-muted);
  --el-input-bg-color: rgba(17, 20, 27, 0.6);
  --el-input-border-color: rgba(var(--accent-color-primary-rgb), 0.3);
  --el-input-hover-border-color: var(--accent-color-primary);
  --el-input-focus-border-color: var(--accent-color-primary);
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 1.1rem;
  font-weight: bold;
  background: var(--accent-color-primary);
  color: var(--text-primary);
  border: none;
}
.register-btn:hover {
  background: var(--accent-color-hover);
  color: var(--text-primary);
  box-shadow: 0 0 20px rgba(var(--accent-color-primary-rgb), 0.3);
}

.register-footer { text-align: center; margin-top: 10px; font-size: 0.9rem; }
.login-link { color: var(--accent-color-primary); font-weight: bold; text-decoration: none; }
.login-link:hover { text-decoration: underline; }

/* 动画 */
@keyframes slide-in-bottom { from { transform: translateY(50px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>