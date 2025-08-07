<template>
  <div class="article-list-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="logo-container">
          <span class="logo-text">码志</span>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <!-- 假设博客页为当前页，添加 active class 用于样式 -->
          <router-link to="/articles" class="nav-link active">博客</router-link>
          <router-link to="/download" class="nav-link">下载</router-link>
          <router-link to="/learn" class="nav-link">学习</router-link>
          <router-link to="/community" class="nav-link">社区</router-link>
        </nav>
        <div class="user-section">
          <button class="icon-btn" @click="goToNotifications" title="消息通知">
            <i class="el-icon-bell"></i>
          </button>
          <img :src="avatarWithTimestamp" class="user-avatar" @click="goToProfile" alt="用户头像" title="个人主页" />
          <span class="username">{{ currentUser }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
      </div>
    </header>

    <!-- 页面控制中心 (集成了搜索和标签) -->
    <div class="page-controls">
      <div class="search-bar">
        <i class="el-icon-search search-icon"></i>
        <input
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
            class="search-input"
            placeholder="探索文章、发现新知..."
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
        <button class="publish-btn" @click="goToPublishPage">发布文章</button>
      </div>
      <div class="tag-bar">
        <button :class="['tag-btn', { active: selectedTagId === null }]" @click="filterByTag(null)">
          全部
        </button>
        <button
            v-for="tag in tags"
            :key="tag.id"
            :class="['tag-btn', { active: selectedTagId === tag.id }]"
            @click="filterByTag(tag.id)"
        >
          # {{ tag.name }}
        </button>
      </div>
    </div>

    <!-- 主内容区域：文章列表 -->
    <main class="main-content">
      <div v-if="loading" class="state-indicator loading">
        <div class="spinner"></div>
        加载中，请稍候...
      </div>
      <div v-else-if="error" class="state-indicator error">
        <i class="el-icon-warning-outline"></i> {{ error }}
      </div>
      <div v-else-if="articles.length" class="article-grid">
        <div
            v-for="article in articles"
            :key="article.id"
            class="article-card"
        >
          <router-link :to="`/article/${article.id}`" class="article-link">
            <div class="card-content">
              <h2 class="article-title">{{ article.title }}</h2>
              <p class="article-excerpt" v-html="truncateContent(article.content)"></p>
            </div>
            <div class="card-footer">
              <div class="author-info">
                <span>{{ article.authorName }}</span>
              </div>
              <div class="article-meta">
                <span>{{ formatDate(article.createTime) }}</span>
                <span>·</span>
                <span>{{ article.viewCount }} 次浏览</span>
              </div>
            </div>
          </router-link>
        </div>
      </div>
      <div v-else class="state-indicator no-articles">
        <i class="el-icon-document-delete"></i>
        暂无文章，敬请期待
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios'
import dayjs from 'dayjs'

export default {
  name: 'ArticleList',
  data() {
    return {
      articles: [],
      tags: [],
      selectedTagId: null,
      loading: false,
      error: null,
      searchKeyword: '',
      avatarTimestamp: Date.now()
    }
  },
  computed: {
    currentUser() {
      return localStorage.getItem("username") || '游客'
    },
    avatarWithTimestamp() {
      const avatarPath = localStorage.getItem("avatar") || ''
      return avatarPath ? `http://localhost:8081${avatarPath}?t=${this.avatarTimestamp}` : ''
    }
  },
  methods: {
    formatDate(dateString) {
      return dayjs(dateString).format('YYYY-MM-DD HH:mm')
    },
    truncateContent(content) {
      if (!content) return ''
      // 只保留前100字，去除html标签
      const text = content.replace(/<[^>]+>/g, '')
      return text.length > 100 ? text.substring(0, 100) + '...' : text
    },
    logout() {
      localStorage.clear()
      this.$router.push("/login")
    },
    async getArticles(tagId = null) {
      this.loading = true
      this.error = null
      try {
        let url = tagId
            ? `http://localhost:8081/article/ArticleByTag?TagID=${tagId}`
            : 'http://localhost:8081/article/articleList'
        const res = await axios.get(url, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`
          }
        })
        if (res.data.code === "0") {
          this.articles = res.data.data
        } else {
          this.error = res.data.msg || '加载失败'
        }
      } catch (error) {
        this.error = '请求失败: ' + error.message
      } finally {
        this.loading = false
      }
    },
    async getTags() {
      try {
        const res = await axios.get('http://localhost:8081/tag/TagList', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`
          }
        })
        if (res.data.code === "0") {
          this.tags = res.data.data
        }
      } catch (error) {
        // 忽略标签加载失败
      }
    },
    filterByTag(tagId) {
      this.selectedTagId = tagId
      this.getArticles(tagId)
    },
    goToPublishPage() {
      this.$router.push('/publish')
    },
    goToProfile() {
      const userId = localStorage.getItem('userId')
      if (userId) {
        this.$router.push(`/user/${userId}`)
      } else {
        this.$router.push('/login')
      }
    },
    goToNotifications() {
      this.$router.push('/notification')
    },
    // 搜索功能
    async handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.getArticles(this.selectedTagId)
        return
      }
      this.loading = true
      this.error = null
      try {
        const res = await axios.get('http://localhost:8081/article/search', {
          params: {
            keyword: this.searchKeyword,
            page: 1,
            size: 20
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`
          }
        })
        if (res.data.code === "0") {
          this.articles = res.data.data
        } else {
          this.error = res.data.msg || '搜索失败'
        }
      } catch (error) {
        this.error = '搜索失败: ' + error.message
      } finally {
        this.loading = false
      }
    }
  },
  created() {
    this.getArticles()
    this.getTags()
  }
}
</script>

<style scoped>
/* 引入外部字体 (关键) */
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap');

/* ================= 1. 定义与登录页一致的暗黑主题变量 ================= */
:root {
  --bg-dark-primary: #111827;
  --bg-dark-secondary: #1f2937;
  --bg-card: rgba(31, 41, 55, 0.5);
  --text-primary: #f9fafb;
  --text-secondary: #e5e7eb;
  --text-muted: #9ca3af;
  --accent-color-primary: #818cf8;
  --accent-color-hover: #6366f1;
  --border-color: rgba(129, 140, 248, 0.2);
  --shadow-color: rgba(129, 140, 248, 0.3);
  --border-radius-card: 12px;
  --border-radius-btn: 8px;
}

/* ================= 2. 页面基础样式更新 ================= */
.article-list-page {
  min-height: 100vh;
  /* 应用暗黑背景 */
  background: var(--bg-dark-primary);
  /* 统一字体 */
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ================= 3. Header 导航栏视觉革新 ================= */
.header {
  /* 玻璃拟态效果 */
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(12px);
  box-shadow: 0 1px 0 var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  height: 72px; /* 增加高度以获得更好的空间感 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

/* 关键：应用书法字体到 Logo */
.logo-text {
  font-family: 'Ma Shan Zheng', cursive;
  font-size: 2.5rem; /* 调整大小以匹配字体风格 */
  font-weight: normal; /* 书法字体通常不需要额外加粗 */
  /* 移除渐变色，使用纯色以配合暗黑主题 */
  background: none;
  -webkit-background-clip: unset;
  -webkit-text-fill-color: var(--text-primary);
  color: var(--text-primary);
  letter-spacing: 0.05em; /* 增加字间距 */
  padding-right: 0; /* 移除内边距 */
  position: static;
}

/* 隐藏原有的装饰点，因为书法字体本身已足够有特色 */
.logo-dot {
  display: none;
}

.nav {
  display: flex;
  gap: 32px;
  margin-left: 60px;
}

.nav-link {
  color: var(--text-muted); /* 更新颜色 */
  text-decoration: none;
  font-size: 1rem;
  font-weight: 500;
  padding: 8px 0;
  transition: all 0.2s ease;
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: -2px; /* 调整位置以适应新 header 高度 */
  left: 0;
  width: 0;
  height: 2px;
  /* 应用新的强调色 */
  background: var(--accent-color-primary);
  transition: width 0.3s ease;
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link:hover::after {
  width: 100%;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 1.25rem; /* 增大图标尺寸 */
  padding: 8px;
  border-radius: 50%; /* 改为圆形按钮 */
  transition: all 0.2s ease;
}

.notification-btn:hover {
  background: rgba(129, 140, 248, 0.1);
  color: var(--accent-color-primary);
}

.notification-text {
  /* 在新设计中可以隐藏文字，让界面更简洁 */
  display: none;
}

.user-avatar {
  width: 40px; /* 增大头像 */
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-color); /* 应用主题边框色 */
  box-shadow: none; /* 移除原有阴影 */
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 0 15px var(--shadow-color); /* 应用主题光晕 */
}

.username {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 1rem;
}

.logout-btn {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-btn);
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: var(--accent-color-primary);
  color: var(--text-primary);
  border-color: var(--accent-color-primary);
  transform: translateY(0); /* 移除原有动画 */
  box-shadow: 0 0 10px var(--shadow-color);
}

/* ================= 4. 控制区域样式更新 ================= */
.search-bar {
  background: var(--bg-dark-secondary); /* 新背景色 */
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 400px;
  height: 48px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-btn);
  padding: 0 16px;
  font-size: 1rem;
  background: var(--bg-dark-primary); /* 新背景色 */
  color: var(--text-primary);
  transition: all 0.2s ease;
}

.search-input:focus {
  background: var(--bg-dark-primary);
  border-color: var(--accent-color-primary);
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.2);
  outline: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-btn, .publish-btn {
  height: 48px;
  padding: 0 24px;
  border: none;
  border-radius: var(--border-radius-btn);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn {
  background: var(--accent-color-hover);
  color: var(--text-primary);
}

.publish-btn {
  background: var(--accent-color-primary);
  color: var(--text-primary);
}

.search-btn:hover, .publish-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0, 0.3);
}

.tag-bar {
  background: var(--bg-dark-secondary); /* 与搜索区背景一致 */
  padding: 16px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
  gap: 12px;
  overflow-x: auto;
}

.tag-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 0.9rem;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tag-btn:hover {
  background: transparent;
  border-color: var(--accent-color-hover);
  color: var(--accent-color-hover);
  transform: translateY(0);
}

.tag-btn.active {
  background: var(--accent-color-primary);
  border-color: var(--accent-color-primary);
  color: var(--text-primary);
  box-shadow: 0 0 15px var(--shadow-color);
}

/* ================= 5. 文章列表卡片样式更新 ================= */
.main-content {
  max-width: 1200px; /* 适当加宽 */
  margin: 48px auto;
  padding: 0 32px;
}

.article-list {
  display: grid; /* 改为网格布局 */
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); /* 响应式列数 */
  gap: 24px;
}

.article-card {
  background: var(--bg-card);
  border-radius: var(--border-radius-card);
  overflow: hidden;
  box-shadow: none;
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  border-color: var(--accent-color-primary);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  padding: 24px;
}

.article-title {
  font-size: 1.25rem;
  color: var(--text-primary);
  margin: 0 0 12px;
  font-weight: 600;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.article-content {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

/* ================= 6. 加载/错误状态样式更新 ================= */
.loading, .error, .no-articles {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80px 0;
  color: var(--text-muted);
  font-size: 1.1rem;
}
.error { color: #f87171; } /* 柔和的红色 */


/* 移除原有的 pulse 动画 */
@keyframes pulse {
  /* no-op */
}

/* 响应式调整保持不变 */
@media (max-width: 768px) {
  /* ... 原有的响应式代码 ... */
}
</style>