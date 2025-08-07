<template>
  <div class="today-comments-container">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <h2>今日评论统计</h2>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </div>
      </template>
      <div class="statistics-content">
        <div class="stat-item">
          <div class="stat-value">{{ totalComments }}</div>
          <div class="stat-label">今日评论总数</div>
        </div>
      </div>
    </el-card>

    <el-card class="comments-list-card">
      <template #header>
        <div class="card-header">
          <h2>评论列表</h2>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索评论内容"
              prefix-icon="el-icon-search"
              clearable
              @clear="fetchComments"
              @input="handleSearch"
              class="search-input"
            />
          </div>
        </div>
      </template>

      <el-table
        :data="comments"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column label="评论内容" min-width="200">
          <template #default="{ row }">
            <div class="comment-content">
              <div class="comment-text">{{ row.content }}</div>
              <div class="article-info">
                评论文章：
                <router-link :to="'/article/' + row.articleId" class="article-link">
                  {{ row.articleTitle }}
                </router-link>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论者" min-width="120">
          <template #default="{ row }">
            <div class="user-info">
              <img :src="getAvatarUrl(row.avatar)" class="user-avatar" alt="用户头像" />
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="评论时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewArticle(row.articleId)"
              link
            >
              查看文章
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="totalComments"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const currentPage = ref(1)
const pageSize = ref(10)
const totalComments = ref(0)
const comments = ref([])
const loading = ref(false)
const searchKeyword = ref('')

// 获取今日评论列表
const fetchComments = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8081/admin/GetTodayComment', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    if (res.data.code === '0') {
      comments.value = res.data.data.records
      totalComments.value = res.data.data.total
    } else {
      ElMessage.error(res.data.msg || '获取评论列表失败')
    }
  } catch (error) {
    ElMessage.error('获取评论列表失败')
    console.error('获取评论失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  await fetchComments()
  ElMessage.success('数据已刷新')
}

// 处理页码变化
const handlePageChange = (newPage) => {
  currentPage.value = newPage
  fetchComments()
}

// 查看文章详情
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取头像URL
const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) {
    return '/default-avatar.png'
  }
  if (avatarPath.startsWith('http')) {
    return avatarPath
  }
  if (avatarPath.startsWith('/')) {
    return `http://localhost:8081${avatarPath}`
  }
  return `http://localhost:8081/${avatarPath}`
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value) {
    comments.value = comments.value.filter(comment =>
      comment.content.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  } else {
    fetchComments()
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.today-comments-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.statistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.statistics-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.comments-list-card {
  flex: 1;
}

.header-right {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 200px;
}

.comment-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-text {
  color: #303133;
  line-height: 1.5;
}

.article-info {
  font-size: 13px;
  color: #909399;
}

.article-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.article-link:hover {
  text-decoration: underline;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #EBEEF5;
}

:deep(.el-table) {
  margin-top: 10px;
}

:deep(.el-button--text) {
  padding: 0 8px;
}

:deep(.el-pagination) {
  margin-top: 20px;
  justify-content: center;
}
</style> 