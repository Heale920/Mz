<template>
  <div class="comment-management">
    <!-- 页面标题和筛选区域 -->
    <div class="page-header">
      <div class="header-content">
        <h2>评论管理</h2>
        <div class="filter-section">
          <el-select v-model="filterStatus" placeholder="评论状态" @change="loadComments" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="1" />
            <el-option label="待审核" value="2" />
            <el-option label="已删除" value="0" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadComments"
            clearable
          />
          <el-input
            v-model="searchKeyword"
            placeholder="搜索评论内容或用户名"
            @input="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <el-card class="list-card">
        <el-table 
          :data="comments" 
          style="width: 100%" 
          v-loading="loading"
          border
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="用户名" width="120" align="center">
            <template #default="scope">
              <div class="user-info">
                <el-avatar :size="24" :src="`http://localhost:8081${scope.row.avatar}`" />
                <span>{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="articleTitle" label="文章标题" min-width="200">
            <template #default="scope">
              <el-link type="primary" @click="viewArticle(scope.row.articleId)">
                {{ scope.row.articleTitle }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评论内容" min-width="300">
            <template #default="scope">
              <div class="comment-content">
                <div class="main-content">{{ scope.row.content }}</div>
                <div v-if="scope.row.parentId" class="reply-info">
                  <el-tag size="small" type="info">回复：</el-tag>
                  {{ scope.row.parentContent }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" width="160" align="center">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" effect="light">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button 
                  v-if="scope.row.status === 2"
                  type="success" 
                  size="small" 
                  @click="approveComment(scope.row)"
                  :icon="Check"
                >
                  通过
                </el-button>
                <el-button 
                  v-if="scope.row.status === 1"
                  type="warning" 
                  size="small" 
                  @click="rejectComment(scope.row)"
                  :icon="Close"
                >
                  驳回
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="replyToComment(scope.row)"
                  :icon="ChatDotRound"
                >
                  回复
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteComment(scope.row)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </el-card>
    </div>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评论"
      width="500px"
      destroy-on-close
    >
      <el-form :model="replyForm" label-width="100px">
        <el-form-item label="原评论">
          <div class="original-comment">
            <div class="user-info">
              <el-avatar :size="24" :src="`http://localhost:8081${currentComment?.avatar}`" />
              <span>{{ currentComment?.username }}</span>
            </div>
            <div class="comment-text">{{ currentComment?.content }}</div>
          </div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Check, Close, ChatDotRound, Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()

// 状态和筛选
const comments = ref([])
const loading = ref(false)
const filterStatus = ref('')
const dateRange = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 回复对话框
const replyDialogVisible = ref(false)
const currentComment = ref(null)
const replyForm = ref({
  content: ''
})

// 初始化加载
onMounted(() => {
  loadComments()
})

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: filterStatus.value || null,
      keyword: searchKeyword.value || null,
      startDate: dateRange.value?.[0] ? dayjs(dateRange.value[0]).format('YYYY-MM-DD') : null,
      endDate: dateRange.value?.[1] ? dayjs(dateRange.value[1]).format('YYYY-MM-DD') : null
    }
    
    const response = await axios.get('http://localhost:8081/admin/comments', {
      params,
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === '0') {
      comments.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      ElMessage.error(response.data.msg || '加载评论失败')
      comments.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  loadComments()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadComments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadComments()
}

// 查看文章
const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

// 审核评论
const approveComment = async (comment) => {
  try {
    const response = await axios.put(`http://localhost:8081/admin/comments/${comment.id}/approve`, null, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === '0') {
      ElMessage.success('评论已通过审核')
      loadComments()
    } else {
      ElMessage.error(response.data.msg || '操作失败')
    }
  } catch (error) {
    console.error('审核评论失败:', error)
    ElMessage.error('审核失败')
  }
}

// 驳回评论
const rejectComment = async (comment) => {
  try {
    const response = await axios.put(`http://localhost:8081/admin/comments/${comment.id}/reject`, null, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === '0') {
      ElMessage.success('评论已驳回')
      loadComments()
    } else {
      ElMessage.error(response.data.msg || '操作失败')
    }
  } catch (error) {
    console.error('驳回评论失败:', error)
    ElMessage.error('驳回失败')
  }
}

// 删除评论
const deleteComment = (comment) => {
  ElMessageBox.confirm(
    '确定要删除这条评论吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const response = await axios.delete(`http://localhost:8081/admin/comments/${comment.id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      })
      
      if (response.data.code === '0') {
        ElMessage.success('评论已删除')
        loadComments()
      } else {
        ElMessage.error(response.data.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 回复评论
const replyToComment = (comment) => {
  currentComment.value = comment
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    const response = await axios.post('http://localhost:8081/admin/comments/reply', {
      parentId: currentComment.value.id,
      articleId: currentComment.value.articleId,
      content: replyForm.value.content
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.data.code === '0') {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      replyForm.value.content = ''
      loadComments()
    } else {
      ElMessage.error(response.data.msg || '回复失败')
    }
  } catch (error) {
    console.error('回复评论失败:', error)
    ElMessage.error('回复失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    '0': 'danger',
    '1': 'success',
    '2': 'warning'
  }
  return types[status?.toString()] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    '0': '已删除',
    '1': '正常',
    '2': '待审核'
  }
  return texts[status?.toString()] || '未知'
}
</script>

<style scoped>
.comment-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.page-header h2 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 20px;
}

.filter-section {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-section .el-select,
.filter-section .el-date-picker {
  width: 200px;
}

.filter-section .el-input {
  width: 300px;
}

.comment-list {
  margin-top: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-content {
  padding: 8px 0;
}

.main-content {
  margin-bottom: 8px;
  line-height: 1.5;
}

.reply-info {
  font-size: 13px;
  color: #909399;
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-top: 8px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.original-comment {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.original-comment .user-info {
  margin-bottom: 8px;
}

.original-comment .comment-text {
  color: #606266;
  line-height: 1.5;
}

:deep(.el-table) {
  --el-table-border-color: #ebeef5;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-button--small) {
  padding: 6px 12px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style>