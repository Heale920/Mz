<template>
  <div v-if="loading" class="loading-container">
    <div class="loading-spinner"></div>
    <span>加载中...</span>
  </div>
  <div v-else class="main-content">
    <!-- 统计面板 -->
    <div class="stats-panel">
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalArticles }}</div>
        <div class="stat-label">文章总数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalViews }}</div>
        <div class="stat-label">总浏览量</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalLikes }}</div>
        <div class="stat-label">总点赞数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalFavorites }}</div>
        <div class="stat-label">收藏文章</div>
      </div>
    </div>

    <!-- 内容标签页 -->
    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane label="个人资料" name="profile">
        <div class="profile-card">
          <div class="profile-header">
            <h2>{{ user.username }} 的个人资料</h2>
          </div>
          <div class="profile-body">
            <div class="avatar-section">
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar-large"/>
              <div v-else class="avatar-placeholder">
                <span>{{ user.username.charAt(0).toUpperCase() }}</span>
              </div>
              <div class="avatar-actions">
                <label class="file-upload-btn">
                  <input type="file" @change="onFileChange" accept="image/*" hidden/>
                  选择头像
                </label>
                <button
                    @click="uploadAvatar"
                    :disabled="!selectedFile"
                    class="upload-btn"
                >
                  上传头像
                </button>
              </div>
            </div>
            <div class="profile-form">
              <div class="form-group">
                <label>用户名</label>
                <input
                    v-model="user.username"
                    type="text"
                    placeholder="请输入用户名"
                    class="form-input"
                />
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input
                    v-model="user.email"
                    type="email"
                    placeholder="请输入邮箱"
                    class="form-input"
                />
              </div>
              <div class="account-status">
                <span class="status-label">账户状态：</span>
                <span :class="['status-badge', user.status === 1 ? 'active' : 'inactive']">
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
              </div>
            </div>
          </div>
          <div class="profile-footer">
            <button @click="submitChanges" class="save-btn">
              保存更改
            </button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的文章" name="articles">
        <div class="works-section">
          <h3>我的作品</h3>
          <ul>
            <li
                v-for="article in paginatedArticles"
                :key="article.id"
                class="article-card"
            >
              <router-link :to="`/article/${article.id}`" class="article-link">
                <h3 class="article-title">
                  {{ article.title }}
                  <span v-if="article.isTop" class="top-tag">置顶</span>
                </h3>
                <p class="article-meta">
                  <span>创建时间：{{ formatDate(article.createTime) }}</span>
                </p>
                <p class="article-stats">
                  👁️ 浏览量：{{ article.viewCount }} &nbsp;&nbsp; 👍 点赞：{{ article.likeCount }}
                </p>
                <div class="article-content" v-html="truncateContent(article.content)"></div>
              </router-link>
            </li>
          </ul>
          <!-- 分页控件 -->
          <div class="pagination" v-if="totalPages > 1">
            <button
                :disabled="currentPage === 1"
                @click="currentPage--"
                class="pagination-btn"
            >
              上一页
            </button>
            <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
            <button
                :disabled="currentPage === totalPages"
                @click="currentPage++"
                class="pagination-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="私密文章" name="private">
        <div class="works-section">
          <h3>私密作品</h3>
          <ul>
            <li
                v-for="article in paginatedPrivateArticles"
                :key="article.id"
                class="article-card"
            >
              <router-link :to="`/article/${article.id}`" class="article-link">
                <h3 class="article-title">
                  {{ article.title }}
                  <span class="private-tag">私密</span>
                  <span v-if="article.isTop" class="top-tag">置顶</span>
                </h3>
                <p class="article-meta">
                  <span>创建时间：{{ formatDate(article.createTime) }}</span>
                </p>
                <p class="article-stats">
                  👁️ 浏览量：{{ article.viewCount }} &nbsp;&nbsp; 👍 点赞：{{ article.likeCount }}
                </p>
                <div class="article-content" v-html="truncateContent(article.content)"></div>
              </router-link>
            </li>
          </ul>
          <!-- 私密文章分页 -->
          <div class="pagination" v-if="totalPrivatePages > 1">
            <button
                :disabled="currentPrivatePage === 1"
                @click="currentPrivatePage--"
                class="pagination-btn"
            >
              上一页
            </button>
            <span class="page-info">{{ currentPrivatePage }} / {{ totalPrivatePages }}</span>
            <button
                :disabled="currentPrivatePage === totalPrivatePages"
                @click="currentPrivatePage++"
                class="pagination-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收藏列表" name="favorites">
        <div class="favorites-section">
          <h3>我的收藏</h3>
          <ul>
            <li v-for="favorite in paginatedFavorites" :key="favorite.id" class="article-card">
              <router-link :to="`/article/${favorite.articleId}`" class="article-link">
                <h3 class="article-title">{{ favorite.article?.title }}</h3>
                <p class="article-meta">
                  <span>收藏时间：{{ formatDate(favorite.createdAt) }}</span>
                </p>
                <p class="article-stats">
                  👁️ 浏览量：{{ favorite.article?.viewCount }} &nbsp;&nbsp;
                  👍 点赞：{{ favorite.article?.likeCount }} &nbsp;&nbsp;
                  ⭐ 收藏：{{ favorite.article?.favoriteCount }}
                </p>
                <div class="article-content" v-html="truncateContent(favorite.article?.content)"></div>
              </router-link>
              <div class="article-actions">
                <el-button
                    type="danger"
                    size="small"
                    @click="removeFavorite(favorite.articleId)"
                >
                  取消收藏
                </el-button>
              </div>
            </li>
          </ul>
          <!-- 收藏列表分页 -->
          <div class="pagination" v-if="totalFavoritePages > 1">
            <button
                :disabled="currentFavoritePage === 1"
                @click="currentFavoritePage--"
                class="pagination-btn"
            >
              上一页
            </button>
            <span class="page-info">{{ currentFavoritePage }} / {{ totalFavoritePages }}</span>
            <button
                :disabled="currentFavoritePage === totalFavoritePages"
                @click="currentFavoritePage++"
                class="pagination-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElTabs, ElTabPane, ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from "dayjs"

const router = useRouter()
const userStore = useUserStore()

// 分页相关的状态
const currentPage = ref(1)
const pageSize = ref(5)
const currentFavoritePage = ref(1)
const favoritesPageSize = ref(5)
const activeTab = ref('profile')
const currentPrivatePage = ref(1)
const privatePageSize = ref(5)

const user = ref({
  id: '',
  username: '',
  email: '',
  avatar: '',
  status: 1
})

const loading = ref(true)
const selectedFile = ref(null)
const avatarTimestamp = ref(Date.now())
const articles = ref([])
const favorites = ref([])
const privateArticles = ref([])

// 统计数据
const stats = ref({
  totalArticles: '',
  totalViews: '',
  totalLikes: '',
  totalComments: '',
  totalFavorites: ''
})

// 获取统计数据
async function fetchStats() {
  try {
    console.log(user.value.id)
    const res = await axios.get(`http://localhost:8081/article/stats/${user.value.id}`, {
      headers: {
        Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
      }
    })
    console.log("后端所返回的数据：" , res.data.data)
    if (res.data.code === '0') {
      stats.value = res.data.data
      console.log(stats.value)
    } else {
      ElMessage.error(res.data.msg || '获取统计数据失败')
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

// 获取用户信息
async function fetchUserInfo() {
  try {
    const res = await axios.get(
        `http://localhost:8081/user/currentUser?username=${userStore.username}`,
        {
          headers: {
            Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
          }
        }
    )

    if (res.data.code === '0') {
      Object.assign(user.value, res.data.data)
    } else {
      ElMessage.error(res.data.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 获取用户作品信息
async function fetchUserArticles() {
  try {
    const res = await axios.get(
        `http://localhost:8081/article/user/${user.value.id}`,
        {
          headers: {
            Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
          }
        }
    )
    if (res.data && res.data.code === '0') {
      // 区分公开和私密文章，并按置顶和时间排序
      const allArticles = res.data.data
      
      // 公开文章：先按置顶排序，再按时间排序
      articles.value = allArticles
        .filter(article => article.visibility !== 'PRIVATE')
        .sort((a, b) => {
          // 如果置顶状态不同，置顶的排在前面
          if (a.isTop !== b.isTop) {
            return b.isTop ? 1 : -1
          }
          // 如果置顶状态相同，按时间倒序排列
          return new Date(b.createTime) - new Date(a.createTime)
        })

      // 私密文章：同样先按置顶排序，再按时间排序
      privateArticles.value = allArticles
        .filter(article => article.visibility === 'PRIVATE')
        .sort((a, b) => {
          if (a.isTop !== b.isTop) {
            return b.isTop ? 1 : -1
          }
          return new Date(b.createTime) - new Date(a.createTime)
        })
    } else {
      ElMessage.error(res.data?.msg || '获取作品信息失败')
    }
  } catch (error) {
    ElMessage.error('加载作品信息失败')
  }
}

// 获取用户的收藏列表
async function fetchUserFavorites() {
  console.log("正在获取用户收藏列表，用户ID:", user.value.id);
  try {
    const res = await axios.get(`http://localhost:8081/favorite/list/${user.value.id}`, {
      headers: {
        Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
      }
    });
    console.log("收藏列表响应:", res.data);
    if (res.data.code === '0') {
      favorites.value = res.data.data;
      console.log("成功获取收藏列表，数量:", favorites.value.length);
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    ElMessage.error('获取收藏列表失败');
  }
}

// 取消收藏
async function removeFavorite(articleId) {
  try {
    const res = await axios.delete(`http://localhost:8081/favorite/${articleId}`, {
      headers: {
        Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
      }
    })
    if (res.data.code === '0') {
      ElMessage.success('取消收藏成功')
      await fetchUserFavorites()
    }
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

// 初始化
onMounted(async () => {
  user.value.id = userStore.userId || localStorage.getItem('userId')
  if (!user.value.id) {
    ElMessage.warning('请先登录')
    return router.push('/login')
  }
  await fetchUserInfo()
  await fetchUserArticles()
  await fetchUserFavorites()
  await fetchStats()
  loading.value = false
})

// 头像 URL computed
const avatarUrl = computed(() =>
    user.value.avatar
        ? `http://localhost:8081${user.value.avatar}?t=${avatarTimestamp.value}`
        : ''
)

function onFileChange(e) {
  selectedFile.value = e.target.files[0]
  if (selectedFile.value) {
    avatarTimestamp.value = Date.now()
    user.value.avatar = URL.createObjectURL(selectedFile.value)
  }
}

// 更新用户信息函数
async function updateUser(formData) {
  try {
    const res = await axios.put(
        `http://localhost:8081/user/${user.value.id}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
          }
        }
    )
    if (res.data.code === '0') {
      await fetchUserInfo()
      avatarTimestamp.value = Date.now()
      return true
    } else {
      ElMessage.error(res.data.msg || '请求失败')
      return false
    }
  } catch (error) {
    ElMessage.error('请求过程中发生错误')
    return false
  }
}

// 上传头像并刷新用户信息
async function uploadAvatar() {
  if (!selectedFile.value) return ElMessage.warning('请选择头像')
  const formData = new FormData()
  formData.append('avatar', selectedFile.value)
  formData.append('user', new Blob([JSON.stringify({
    id: user.value.id,
    username: user.value.username,
    email: user.value.email
  })], {type: 'application/json'}))
  const ok = await updateUser(formData)
  if (ok) {
    ElMessage.success('头像上传成功')
    userStore.setAvatar(user.value.avatar)
    localStorage.setItem('avatar', user.value.avatar)
  }
}

// 提交用户信息
async function submitChanges() {
  const formData = new FormData()
  formData.append('user', new Blob([JSON.stringify({
    id: user.value.id,
    username: user.value.username,
    email: user.value.email
  })], {type: 'application/json'}))
  const ok = await updateUser(formData)
  if (ok) {
    ElMessage.success('用户信息保存成功')
    userStore.username = user.value.username
    localStorage.setItem('username', user.value.username)
    router.push('/dashboard1')
  }
}

// 格式化日期
function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 截取文章内容
function truncateContent(content) {
  if (!content) return ''
  const maxLength = 200
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(articles.value.length / pageSize.value)
})

// 获取当前页的文章
const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return articles.value.slice(start, end)
})

// 计算收藏列表分页
const totalFavoritePages = computed(() => {
  return Math.ceil(favorites.value.length / favoritesPageSize.value)
})

const paginatedFavorites = computed(() => {
  const start = (currentFavoritePage.value - 1) * favoritesPageSize.value
  const end = start + favoritesPageSize.value
  return favorites.value.slice(start, end)
})

// 监听页码变化
watch([articles, currentPage], () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value || 1
  }
})

watch([favorites, currentFavoritePage], () => {
  if (currentFavoritePage.value > totalFavoritePages.value) {
    currentFavoritePage.value = totalFavoritePages.value || 1
  }
})

// 添加私密文章相关的计算属性
const totalPrivatePages = computed(() => {
  return Math.ceil(privateArticles.value.length / privatePageSize.value)
})

const paginatedPrivateArticles = computed(() => {
  const start = (currentPrivatePage.value - 1) * privatePageSize.value
  const end = start + privatePageSize.value
  return privateArticles.value.slice(start, end)
})

// 监听私密文章页码变化
watch([privateArticles, currentPrivatePage], () => {
  if (currentPrivatePage.value > totalPrivatePages.value) {
    currentPrivatePage.value = totalPrivatePages.value || 1
  }
})
</script>

<style scoped>
/* 基础样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #6b7280;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.main-content {
  max-width: 1100px;
  margin: 2rem auto;
  padding: 0 1rem;
}

/* 统计面板样式 */
.stats-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #f3f4f6 0%, #e0e7ff 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.08);
  padding: 2rem 1.5rem;
  margin-bottom: 2rem;
  gap: 2rem;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #3b82f6;
  margin-bottom: 0.5rem;
  letter-spacing: 1px;
}

.stat-label {
  font-size: 1rem;
  color: #6b7280;
  margin-top: 0.25rem;
  letter-spacing: 1px;
}

/* 卡片样式 */
.profile-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.profile-header {
  padding: 1.5rem;
  border-bottom: 1px solid #f3f4f6;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

.profile-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 1.5rem;
  font-weight: 600;
}

.profile-body {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

@media (min-width: 768px) {
  .profile-body {
    flex-direction: row;
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  flex: 0 0 200px;
}

.avatar-large {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.avatar-placeholder {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 3rem;
  font-weight: bold;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
}

.file-upload-btn {
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  color: #4b5563;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.file-upload-btn:hover {
  background: #e5e7eb;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.upload-btn:hover {
  background: #2563eb;
}

.upload-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.profile-form {
  flex: 1;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4b5563;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.account-status {
  display: flex;
  align-items: center;
  margin-top: 2rem;
}

.status-label {
  color: #4b5563;
  font-weight: 500;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
}

.profile-footer {
  padding: 1.5rem;
  border-top: 1px solid #f3f4f6;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  padding: 0.75rem 1.5rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn:hover {
  background: #059669;
}

.save-btn:disabled {
  background: #6ee7b7;
  cursor: not-allowed;
}

/* 文章列表样式 */
.works-section {
  margin-top: 2rem;
}

.works-section h3 {
  margin-bottom: 1rem;
  color: #1f2937;
  font-size: 1.25rem;
  font-weight: 600;
}

.works-section ul {
  list-style: none;
  padding: 0;
}

.article-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.article-link {
  display: block;
  padding: 1.5rem;
  text-decoration: none;
  color: inherit;
}

.article-title {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: #1f2937;
  font-weight: 600;
}

.article-meta {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.article-stats {
  font-size: 0.875rem;
  color: #4b5563;
  margin-bottom: 1rem;
}

.article-content {
  color: #4b5563;
  font-size: 0.875rem;
  line-height: 1.5;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
  gap: 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.pagination-btn:hover:not(:disabled) {
  background: #2563eb;
}

.pagination-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.875rem;
  color: #4b5563;
}

/* 收藏列表样式 */
.favorites-section {
  margin-top: 1rem;
}

.article-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0.5rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

/* 标签页样式 */
.content-tabs {
  margin-top: 2rem;
}

.private-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
  background-color: #e6a23c;
  color: white;
}

.top-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
  background-color: #409eff;
  color: white;
}

.article-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-title span {
  flex-shrink: 0;
}
</style>