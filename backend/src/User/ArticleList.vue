<template>
  <div class="article-list-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="container">
        <div class="logo">
          <span class="logo-text">MZ</span>
          <div class="logo-dot"></div>
        </div>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/blog" class="nav-link">博客</router-link>
          <router-link to="/download" class="nav-link">下载</router-link>
          <router-link to="/learn" class="nav-link">学习</router-link>
          <router-link to="/community" class="nav-link">社区</router-link>
        </nav>
        <div class="user-section">
          <button class="notification-btn" @click="goToNotifications">
            <i class="el-icon-bell"></i>
            <span class="notification-text">消息</span>
          </button>
          <img :src="avatarWithTimestamp" class="user-avatar" @click="goToProfile" alt="用户头像" />
          <span class="username">{{ currentUser }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
      </div>
    </header>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
          v-model="searchKeyword"
          @keyup.enter="handleSearch"
          class="search-input"
          placeholder="搜索文章标题或内容"
      />
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="publish-btn" @click="goToPublishPage">发布文章</button>
    </div>

    <!-- 标签导航 -->
    <div class="tag-bar">
      <button
          :class="['tag-btn', { active: selectedTagId === null }]"
          @click="filterByTag(null)"
      >全部</button>
      <button
          v-for="tag in tags"
          :key="tag.id"
          :class="['tag-btn', { active: selectedTagId === tag.id }]"
          @click="filterByTag(tag.id)"
      >{{ tag.name }}</button>
    </div>

    <!-- 文章列表 -->
    <main class="main-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="articles.length" class="article-list">
        <div
            v-for="article in articles"
            :key="article.id"
            class="article-card"
        >
          <router-link :to="`/article/${article.id}`" class="article-link">
            <h2 class="article-title">{{ article.title }}</h2>
            <div class="article-meta">
              <span>作者：{{ article.authorName }}</span>
              <span>发布时间：{{ formatDate(article.createTime) }}</span>
              <span>浏览：{{ article.viewCount }}</span>
              <span>点赞：{{ article.likeCount }}</span>
            </div>
            <div class="article-content" v-html="truncateContent(article.content)"></div>
          </router-link>
        </div>
      </div>
      <div v-else class="no-articles">
        暂无文章
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
.article-list-page {
  min-height: 100vh;
  background: #f8f9fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.header {
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo-text {
  font-size: 32px;
  font-weight: 900;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -1px;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  padding-right: 8px;
  position: relative;
}

.logo-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #10b981, #34d399);
  border-radius: 50%;
  position: absolute;
  bottom: 8px;
  right: -4px;
  animation: pulse 2s infinite;
}

.nav {
  display: flex;
  gap: 32px;
  margin-left: 60px;
}

.nav-link {
  color: #4b5563;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 0;
  transition: all 0.2s ease;
  position: relative;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  transition: width 0.2s ease;
}

.nav-link:hover {
  color: #3b82f6;
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
  color: #4b5563;
  cursor: pointer;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.notification-btn:hover {
  background: rgba(59, 130, 246, 0.08);
  color: #3b82f6;
}

.notification-text {
  font-weight: 500;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.username {
  color: #4b5563;
  font-weight: 500;
  font-size: 15px;
}

.logout-btn {
  background: #fff;
  color: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: #2563eb;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

.search-bar {
  background: #fff;
  padding: 24px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 400px;
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 16px;
  font-size: 15px;
  background: #f9fafb;
  transition: all 0.2s ease;
}

.search-input:focus {
  background: #fff;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  outline: none;
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-btn {
  height: 44px;
  padding: 0 20px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.publish-btn {
  height: 44px;
  padding: 0 20px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.tag-bar {
  background: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  display: flex;
  justify-content: center;
  gap: 12px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-bar::-webkit-scrollbar {
  display: none;
}

.tag-btn {
  background: #f3f4f6;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tag-btn:hover {
  background: #2563eb;
  color: #fff;
  transform: translateY(-1px);
}

.tag-btn.active {
  background: #2563eb;
  color: #fff;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

.main-content {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 24px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.article-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.1);
  border-color: rgba(37, 99, 235, 0.1);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  padding: 24px;
}

.article-title {
  font-size: 20px;
  color: #111827;
  margin: 0 0 16px;
  font-weight: 600;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #6b7280;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  font-size: 15px;
  color: #4b5563;
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  color: #6b7280;
  font-size: 15px;
}

.error {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  color: #ef4444;
  font-size: 15px;
}

.no-articles {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  color: #6b7280;
  font-size: 15px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }

  .nav {
    display: none;
  }

  .search-bar {
    padding: 16px;
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }

  .search-btn,
  .publish-btn {
    width: 100%;
  }

  .tag-bar {
    padding: 12px 16px;
  }

  .article-card {
    margin: 0 16px;
  }

  .article-link {
    padding: 20px;
  }

  .article-meta {
    flex-direction: column;
    gap: 8px;
  }

  .logo-text {
    font-size: 28px;
  }
  
  .logo-dot {
    width: 6px;
    height: 6px;
    bottom: 6px;
    right: -2px;
  }
  
  .notification-text {
    display: none;
  }
}
</style>