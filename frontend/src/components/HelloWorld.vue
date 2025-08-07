<template>
  <div class="dashboard-container">
    <nav class="navbar">
      <div class="logo">TechBlog</div>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/login">登录</router-link>
        <router-link to="/register">注册</router-link>
      </div>
    </nav>

    <section class="hero-section">
      <h1>你好，技术探索者 👨‍💻</h1>
      <p>欢迎来到你的专属博客空间，记录你的代码、思考与成长。</p>
      <router-link to="/editor" class="cta-btn">开始写博客</router-link>
    </section>
  </div>
</template>

<style scoped>
.dashboard-container {
  background: #0a192f;
  color: #e6f1ff;
  min-height: 100vh;
  font-family: 'Consolas', monospace;
  padding: 30px;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-bottom: 40px;
}

.logo {
  font-size: 1.5rem;
  color: #64ffda;
}

.nav-links a {
  color: #8892b0;
  margin-left: 20px;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: #64ffda;
}

.hero-section {
  text-align: center;
  margin-top: 100px;
}

.hero-section h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
  color: #64ffda;
}

.hero-section p {
  color: #8892b0;
  font-size: 1.1rem;
  margin-bottom: 40px;
}

.cta-btn {
  padding: 12px 24px;
  background-color: transparent;
  border: 1px solid #64ffda;
  color: #64ffda;
  text-decoration: none;
  font-size: 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.cta-btn:hover {
  background-color: rgba(100, 255, 218, 0.1);
  box-shadow: 0 5px 20px rgba(100, 255, 218, 0.3);
}
</style>
