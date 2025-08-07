<template>
  <div v-if="loading" class="loading-container">
    <div class="loading-spinner"></div>
    <span>加载中...</span>
  </div>
  <div v-else class="profile-container">
    <div class="profile-layout">
      <el-button 
        @click="router.push('/dashboard1')" 
        class="back-btn"
        :icon="ArrowLeft"
        type="info"
        plain
      >
        返回
      </el-button>
      <div class="main-content">
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
            <div class="stat-value">{{ stats.totalComments }}</div>
            <div class="stat-label">总评论数</div>
          </div>
        </div>
        <!-- 个人资料卡片 -->
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
          <!-- 新增作品展示区域 -->
          <div class="works-section">
            <h3>我的作品</h3>
            <ul>
              <li
                  v-for="article in paginatedArticles"
                  :key="article.id"
                  class="article-card"
              >
                <router-link :to="`/article/${article.id}`" class="article-link">
                  <h3 class="article-title">{{ article.title }}</h3>
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, computed, watch} from 'vue'
import axios from 'axios'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {ElMessage} from 'element-plus'
import dayjs from "dayjs";

const router = useRouter()
const userStore = useUserStore()
// 分页相关的状态
const currentPage = ref(1)
const pageSize = ref(5) // 每页显示5篇文章

const user = ref({
  id: '',
  username: '',
  email: '',
  avatar: '',
  status: 1
})
const loading = ref(true)
const selectedFile = ref(null)
const avatarTimestamp = ref(Date.now()) // 时间戳用于防止图片缓存
const articles = ref([]) // 新增作品数据

// 统计数据
const stats = ref({
  totalArticles: 0,
  totalViews: 0,
  totalLikes: 0,
  totalComments: 0
})

// 获取统计数据
async function fetchStats() {
  try {
    const res = await axios.get(`/api/article/stats/${user.value.id}`, {
      headers: {
        Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
      }
    })
    if (res.data.code === '0') {
      Object.assign(stats.value, res.data.data)
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
    console.log('正在获取用户作品，用户ID:', user.value.id);

    const res = await axios.get(
        `/api/article/user/${user.value.id}`,
        {
          headers: {
            Authorization: `Bearer ${userStore.token || localStorage.getItem('token')}`
          }
        },
    );

    console.log('获取作品响应:', res.data);

    if (res.data && res.data.code === '0') {
      articles.value = res.data.data;
      console.log('作品数量:', articles.value.length);
    } else {
      console.error('获取作品失败，错误码:', res.data?.code);
      ElMessage.error(res.data?.msg || '获取作品信息失败');
    }
  } catch (error) {
    console.error('获取作品失败:', error);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
    ElMessage.error('加载作品信息失败');
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
  await fetchUserArticles() // 新增获取作品信息
  await fetchStats() // 新增获取统计数据
  loading.value = false
})

// 头像 URL computed 动态生成url，避免浏览器缓存
const avatarUrl = computed(() =>
    user.value.avatar
        ? `http://localhost:8081${user.value.avatar}?t=${avatarTimestamp.value}`
        : ''
)

function onFileChange(e) {
  selectedFile.value = e.target.files[0]
  if (selectedFile.value) {
    avatarTimestamp.value = Date.now() // 刷新头像显示
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
    console.error('请求错误:', error)
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

// 监听当前页变化，确保在文章数量变化时页码合法
watch([articles, currentPage], () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value || 1
  }
})
</script>

<style scoped>
/* 基础样式 */
.profile-container {
  max-width: 1100px;
  margin: 2rem auto;
  padding: 0 1rem;
}

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

.profile-layout {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  padding: 1.5rem;
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

.works-section li {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1rem;
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

.back-btn {
  position: sticky;
  top: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #4b5563;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.back-btn:hover {
  background: #f9fafb;
  color: #1f2937;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style> 