<template>
  <div id="app-container">
    <!-- 导航栏 -->
    <header class="main-header">
      <div class="container">
        <div class="logo-container">
          <span class="logo-text">码志</span>
        </div>
        <nav class="main-nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/articles" class="nav-link">博客</router-link>
          <router-link to="/about" class="nav-link">关于</router-link>
        </nav>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 路由视图 -->
      <router-view />

      <!-- 首页核心内容 -->
      <div class="hero-content-wrapper" v-if="$route.path === '/'">
        <h1 class="hero-title">始于代码，不止于码</h1>
        <p class="hero-subtitle">一个专注于技术深度与广度的开发者社区。</p>
        <div class="hero-actions">
          <router-link to="/login" class="btn btn-secondary">登录</router-link>
          <router-link to="/register" class="btn btn-primary">即刻加入</router-link>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="main-footer" v-if="$route.path === '/'">
      <div class="container footer-bottom">
        <p>&copy; {{ new Date().getFullYear() }} 码志 (MaZhi.io)</p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
}
</script>

<style>
/* 全局与变量定义 */
:root {
  --primary-color: #6366f1;
  --primary-hover-color: #4f46e5;
  --text-bright: #f9fafb;
  --text-main: #d1d5db;
  --text-muted: #9ca3af;
  --background-dark: #0D0D10;
  --header-height: 85px;
  --primary-color-rgb: 99, 102, 241;
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  --font-serif: 'Lora', serif;
  --radius-md: 8px;
  --transition-ease: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700&family=Lora:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap');

body {
  margin: 0;
  font-family: var(--font-sans);
  color: var(--text-main);
  background-color: var(--background-dark);
  background-image: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(120, 119, 198, 0.3), hsla(0, 0%, 100%, 0));
  background-attachment: fixed;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ================== 核心布局调整 ================== */

#app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh; /* 确保整个容器至少和屏幕一样高 */
  background-color: transparent;
}

/* 这是实现垂直居中的关键 */
.main-content {
  flex-grow: 1; /* 让 main 区域占据 header 和 footer 之外的所有剩余空间 */
  display: flex; /* 将 main 自身也设为 flex 容器 */
  flex-direction: column; /* 内部元素垂直排列 */
  justify-content: center; /* 垂直居中其所有子元素 */
  align-items: center; /* 水平居中其所有子元素 */
}

/* ================== 导航栏 ================== */
.main-header {
  min-height: var(--header-height);
  padding: 1rem 0;
  display: flex;
  align-items: center;
  width: 100%;
  z-index: 1000;
  /* 恢复为非绝对定位，成为正常文档流的一部分 */
}

.main-header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.logo-text {
  font-family: 'Ma Shan Zheng', cursive;
  font-size: 2.25rem;
  font-weight: normal;
  color: var(--text-bright);
  line-height: 1;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  color: var(--text-main);
  text-decoration: none;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  transition: var(--transition-ease);
  font-weight: 500;
}

.nav-link:hover, .nav-link.router-link-exact-active {
  background-color: rgba(255, 255, 255, 0.05);
  color: var(--text-bright);
}


/* ================== 内容区域 ================== */
.hero-content-wrapper {
  text-align: center;
  padding: 24px;
}

.hero-title {
  font-family: var(--font-serif);
  font-size: clamp(3rem, 6vw, 4.5rem);
  color: var(--text-bright);
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-muted);
  max-width: 700px;
  margin: 0 auto 3.5rem;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn {
  padding: 14px 48px;
  font-size: 1rem;
  font-weight: 500;
  text-decoration: none;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1),
  box-shadow 0.15s cubic-bezier(0.4, 0, 0.2, 1),
  background-color 0.15s ease,
  border-color 0.15s ease,
  color 0.15s ease;
}

.btn:hover {
  transform: translateY(-3px);
}

.btn:active {
  transform: translateY(0px) scale(0.98);
  box-shadow: none !important;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}
.btn-primary:hover {
  background-color: var(--primary-hover-color);
  box-shadow: 0 5px 20px rgba(var(--primary-color-rgb), 0.25);
}

.btn-secondary {
  background-color: transparent;
  color: var(--text-main);
  border-color: rgba(255, 255, 255, 0.2);
}
.btn-secondary:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background-color: rgba(var(--primary-color-rgb), 0.1);
}

/* ================== 页脚 ================== */
.main-footer {
  padding: 2rem 0;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 0.875rem;
}

/* ================== 响应式调整 ================== */
@media (max-width: 768px) {
  .main-header .container {
    flex-direction: column;
    gap: 1rem;
  }
  .hero-actions {
    flex-direction: column;
    width: 100%;
    max-width: 300px;
  }
  .btn {
    width: 100%;
  }
}
</style>