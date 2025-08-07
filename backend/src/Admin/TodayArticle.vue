<template>
    <div class="today-article-container">
      <el-card class="statistics-card">
        <template #header>
          <div class="card-header">
            <h2>今日文章统计</h2>
            <el-button type="primary" @click="refreshData">刷新数据</el-button>
          </div>
        </template>
        <div class="statistics-content">
          <div class="stat-item">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">今日发布文章数</div>
          </div>
        </div>
      </el-card>
  
      <el-card class="article-list-card">
        <template #header>
          <div class="card-header">
            <h2>文章列表</h2>
            <div class="header-right">
              <el-input
                  v-model="searchKeyword"
                  placeholder="搜索文章标题"
                  prefix-icon="el-icon-search"
                  clearable
                  @clear="fetchTodayArticles"
                  @input="handleSearch"
                  class="search-input"
              />
            </div>
          </div>
        </template>
  
        <el-table
            :data="articles"
            stripe
            style="width: 100%"
            v-loading="loading"
        >
          <el-table-column label="标题" min-width="200">
            <template #default="{ row }">
              <router-link :to="`/article/${row.id}`" class="article-link">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
  
          <el-table-column prop="authorName" label="作者" min-width="120" />
  
          <el-table-column prop="createTime" label="发布时间" min-width="160">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
  
          <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
  
          <el-table-column prop="likeCount" label="点赞数" width="100" align="center" />
  
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                  type="primary"
                  size="small"
                  @click="viewArticle(row.id)"
                  link
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
  
        <div class="pagination-container">
          <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="page"
              :page-size="size"
              :total="total"
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
  
  const router = useRouter()
  const page = ref(1)
  const size = ref(10)
  const total = ref(0)
  const articles = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')
  
  // 获取今日文章列表
  const fetchTodayArticles = async () => {
    loading.value = true
    try {
      const res = await axios.get('http://localhost:8081/admin/todayArticle', {
        params: {
          page: page.value,
          size: size.value
        }
      })
      console.log('获取到的文章数据:', res.data.data.records)  // 添加调试日志
      articles.value = res.data.data.records
      total.value = res.data.data.total
    } catch (error) {
      ElMessage.error('获取文章列表失败')
      console.error('获取文章失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  // 刷新数据
  const refreshData = async () => {
    await Promise.all([fetchTodayArticles(), fetchDashboardStats()])
    ElMessage.success('数据已刷新')
  }
  
  // 获取统计数据
  const fetchDashboardStats = async () => {
    try {
      const response = await axios.get('http://localhost:8081/admin/count', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      })

      console.log('统计数据接口响应:', response.data)

      if (response.data.code === '0') {
        const data = response.data.data
        if (data) {
          total.value = data.todayPublishedArticleCount || 0
          console.log('设置统计数据:', {
            todayPosts: total.value
          })
        }
      } else {
        console.error('获取统计数据失败:', response.data.msg)
        ElMessage.error(response.data.msg || '获取统计数据失败')
        resetStats()
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
      ElMessage.error('获取统计数据失败: ' + (error.response?.data?.msg || error.message))
      resetStats()
    }
  }
  
  // 重置统计数据
  const resetStats = () => {
    total.value = 0
  }
  
  // 处理页码变化
  const handlePageChange = (newPage) => {
    page.value = newPage
    fetchTodayArticles()
  }
  
  // 查看文章详情
  const viewArticle = (articleId) => {
    router.push(`/article/${articleId}`)
  }
  
  // 格式化日期
  const formatDate = (date) => {
    if (!date) return ''
    const d = new Date(date)
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  // 处理搜索
  const handleSearch = () => {
    if (searchKeyword.value) {
      articles.value = articles.value.filter(article => 
        article.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
    } else {
      fetchTodayArticles()
    }
  }
  
  onMounted(() => {
    fetchTodayArticles()
    fetchDashboardStats()
  })
  </script>
  
  <style scoped>
  .today-article-container {
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
  
  .article-list-card {
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
  
  .article-link {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
  }
  
  .article-link:hover {
    text-decoration: underline;
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
  
  