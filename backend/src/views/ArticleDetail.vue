<template>
  <div class="article-detail" :class="article?.layout || 'default'">
    <div v-if="loading" class="loading-spinner">加载中...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else-if="article">
      <!-- 文章标题和元信息 -->
      <h1 :style="{ color: article.titleColor || '#000000' }">{{ article.title }}</h1>
      <div class="article-meta">
        <span>作者: {{ article.authorName }}</span>
        <span>发布时间: {{ formatDate(article.createTime) }}</span>
        <span>浏览量: {{ article.viewCount }}</span>
        <span v-if="article.isPrivate" class="status-tag private">私密</span>
        <span v-if="article.isTop" class="status-tag top">置顶</span>
      </div>

      <!-- 文章内容 -->
      <div class="content" :class="article.fontSize || 'medium'" v-html="article.content"></div>

      <!-- 文章操作按钮 -->
      <div class="article-operations">
        <!-- 作者操作按钮 -->
        <div v-if="isAuthor" class="author-actions">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon> 编辑文章
          </el-button>
          <el-button type="primary" @click="showSettingsDialog">
            <el-icon><Setting /></el-icon> 文章设置
          </el-button>
          <el-popconfirm
              title="确定要删除这篇文章吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete"
          >
            <template #reference>
              <el-button type="danger">
                <el-icon><Delete /></el-icon> 删除文章
              </el-button>
            </template>
          </el-popconfirm>
        </div>

        <!-- 用户操作按钮 -->
        <div class="user-actions">
          <el-button
              :type="hasLiked ? 'primary' : 'default'"
              @click="toggleLike"
              class="action-button"
          >
            <el-icon><ThumbsUp /></el-icon>
            {{ hasLiked ? '已点赞' : '点赞' }}
            <span class="count">{{ article.likeCount }}</span>
          </el-button>

          <el-button
              :type="hasFavorited ? 'primary' : 'default'"
              @click="toggleFavorite"
              class="action-button"
          >
            <el-icon><Star /></el-icon>
            {{ hasFavorited ? '已收藏' : '收藏' }}
            <span class="count">{{ article.favoriteCount }}</span>
          </el-button>
        </div>
      </div>

      <!-- 文章设置对话框 -->
      <el-dialog
          v-model="settingsVisible"
          title="文章设置"
          width="500px"
      >
        <div class="settings-form">
          <el-form :model="articleSettings" label-width="100px">
            <el-form-item label="文章可见性">
              <el-radio-group v-model="articleSettings.isPrivate" @change = handleVisibility>
                <el-radio :label="false">公开</el-radio>
                <el-radio :label="true">私密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="是否置顶">
              <el-switch v-model="articleSettings.isTop" />
            </el-form-item>

            <el-form-item label="布局方式">
              <el-radio-group v-model="articleSettings.layout">
                <el-radio label="default">默认</el-radio>
                <el-radio label="wide">宽屏</el-radio>
                <el-radio label="centered">居中</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="标题颜色">
              <el-color-picker v-model="articleSettings.titleColor" />
            </el-form-item>

            <el-form-item label="正文字号">
              <el-select v-model="articleSettings.fontSize">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="settingsVisible = false">取消</el-button>
            <el-button type="primary" @click="saveSettings">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 评论模块 -->
      <div class="comments-section">
        <h2>评论</h2>

        <!-- 添加评论框 -->
        <div class="comment-input">
          <textarea v-model="newComment" placeholder="写下你的评论..." rows="3"></textarea>
          <button @click="submitComment">发表评论</button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <img :src="`http://localhost:8081${comment.avatar}`" alt="头像" class="avatar">
              <span class="username">{{ comment.username }}</span>
              <span class="time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>

            <!-- 回复按钮 -->
            <button class="action-btn" @click="toggleReplyBox(comment.id)">
              {{ activeReplyId === comment.id ? '取消回复' : '回复' }}
            </button>

            <!-- 回复输入框 -->
            <div v-if="activeReplyId === comment.id" class="reply-box">
              <textarea v-model="replyContent" placeholder="写下你的回复..."></textarea>
              <div class="reply-actions">
                <button @click="submitReply(comment.id)">提交</button>
                <button @click="cancelReply">取消</button>
              </div>
            </div>

            <!-- 子评论列表 -->
            <div v-if="comment.replies && comment.replies.length" class="replies">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <div class="comment-header">
                  <img :src="`http://localhost:8081${reply.avatar}`" alt="头像" class="avatar">
                  <span class="username">{{ reply.username }}</span>
                  <span class="time">{{ formatDate(reply.createTime) }}</span>
                </div>
                <p class="comment-content">{{ reply.content }}</p>
              </div>
            </div>

            <!-- 加载更多回复按钮 -->
            <button
                v-if="comment.replyCount > 0 && (!comment.replies || comment.replies.length < comment.replyCount)"
                class="load-more-btn"
                @click="loadMoreReplies(comment)"
            >
              {{ comment.replies ? `加载更多回复(${comment.replyCount - comment.replies.length})` : `查看回复(${comment.replyCount})` }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { Edit, Delete, Setting, Star, ThumbsUp } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const loading = ref(true)
const error = ref(null)
const comments = ref([])
const newComment = ref('')
const replyContent = ref('')
const activeReplyId = ref(null)
const hasLiked = ref(false)
const isAuthor = ref(false)
const hasFavorited = ref(false)
const settingsVisible = ref(false)
const articleSettings = ref({
  isPrivate: false,
  isTop: false,
  layout: 'default',
  titleColor: '#000000',
  fontSize: 'medium'
})

const userId = ref(localStorage.getItem('userId') || 1)

// 初始化加载
onMounted(async () => {
  try {
    const articleId = route.params.id
    await loadArticle(articleId)
    await loadComments(articleId)
    await checkFavoriteStatus(articleId)
  } catch (err) {
    error.value = '加载失败，请稍后重试'
    console.error('加载失败:', err)
  } finally {
    loading.value = false
  }
})

// 检查收藏状态
const checkFavoriteStatus = async (articleId) => {
  try {
    const res = await axios.get(`http://localhost:8081/favorite/check/${articleId}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })
    hasFavorited.value = res.data.data
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!localStorage.getItem("token")) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const method = hasFavorited.value ? 'delete' : 'post'
    const res = await axios[method](`http://localhost:8081/favorite/${article.value.id}`, {}, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })

    if (res.data.code === "0") {
      hasFavorited.value = !hasFavorited.value
      article.value.favoriteCount = res.data.data
      ElMessage.success(hasFavorited.value ? '收藏成功' : '已取消收藏')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    if (error.response?.status === 401) {
      router.push('/login')
    }
  }
}

// 显示设置对话框
const showSettingsDialog = () => {
  articleSettings.value = {
    isPrivate: article.value.visibility === 'PRIVATE',
    isTop: article.value.isTop || false,
    layout: article.value.layout || 'default',
    titleColor: article.value.titleColor || '#000000',
    fontSize: article.value.fontSize || 'medium'
  }
  settingsVisible.value = true
}

// 保存文章设置
const saveSettings = async () => {
  try {
    // 1. 保存可见性设置
    await axios.post(
      `http://localhost:8081/article/visibility/${article.value.id}`,
      null,
      {
        params: {
          visibility: articleSettings.value.isPrivate ? 'PRIVATE' : 'PUBLIC'
        },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`
        }
      }
    )

    // 2. 保存置顶状态
    await axios.put(
      `http://localhost:8081/article/${article.value.id}/top`,
      null,
      {
        params: {
          isTop: articleSettings.value.isTop
        },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`
        }
      }
    )

    // 3. 保存样式设置
    await axios.put(
      `http://localhost:8081/article/${article.value.id}/style`,
      null,
      {
        params: {
          layout: articleSettings.value.layout,
          style: JSON.stringify({
            titleColor: articleSettings.value.titleColor,
            fontSize: articleSettings.value.fontSize
          })
        },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`
        }
      }
    )

    // 更新本地文章数据
    Object.assign(article.value, {
      visibility: articleSettings.value.isPrivate ? 'PRIVATE' : 'PUBLIC',
      isPrivate: articleSettings.value.isPrivate,
      isTop: articleSettings.value.isTop,
      layout: articleSettings.value.layout,
      titleColor: articleSettings.value.titleColor,
      fontSize: articleSettings.value.fontSize
    })
    
    ElMessage.success('设置保存成功')
    settingsVisible.value = false
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  }
}

// 加载文章详情
const loadArticle = async (articleId) => {
  try {
    // 获取文章详情
    const res = await axios.get(`http://localhost:8081/article/${articleId}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    });
    article.value = res.data.data;
    // 设置文章的私密状态
    article.value.isPrivate = article.value.visibility === 'PRIVATE';

    // 判断是否为作者
    const currentUserId = localStorage.getItem('userId')
    isAuthor.value = currentUserId && currentUserId == article.value.authorId

    // 检查是否已点赞
    const likeRes = await axios.get(`http://localhost:8081/article/hasLiked/${articleId}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    });
    hasLiked.value = likeRes.data.data;

    // 增加浏览量
    const viewRes = await axios.post(`http://localhost:8081/article/view/${articleId}`, {
      articleId: articleId
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    });

    if (viewRes.data.code === "0") {
      article.value.viewCount = viewRes.data.data;
      console.log('浏览量更新为:', article.value.viewCount);
    } else {
      console.error('更新浏览量失败:', viewRes.data.msg);
    }

  } catch (error) {
    console.error('Error fetching article:', error);
    throw error;
  }
}

// 编辑文章
const handleEdit = () => {
  router.push(`/edit/${article.value.id}`)
}

// 删除文章
const handleDelete = async () => {
  try {
    const res = await axios.delete(`http://localhost:8081/article/${article.value.id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })

    if (res.data.code === "0") {
      ElMessage.success('文章删除成功')
      router.push('/dashboard1')
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除文章失败:', error)
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}
//文章可见性
const handleVisibility = async () => {
  try {
    const res = await axios.post(
      `http://localhost:8081/article/visibility/${article.value.id}`,
      null,
      {
        params: {
          visibility: articleSettings.value.isPrivate ? 'PRIVATE' : 'PUBLIC'
        },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`
        }
      }
    )

    if (res.data.code === "0") {
      ElMessage.success('可见性设置成功！')
      article.value.visibility = articleSettings.value.isPrivate ? 'PRIVATE' : 'PUBLIC'
    } else {
      ElMessage.error(res.data.msg || '设置失败！')
    }
  } catch (error) {
    console.error('设置文章可见性失败:', error)
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('设置失败，请稍后重试')
    }
  }
}

// 点赞功能
const toggleLike = async () => {
  try {
    const res = await axios.post(`http://localhost:8081/article/like/${route.params.id}`, {
      userId: userId.value,
      articleId: route.params.id
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    });

    if (res.data.code === "0") {
      article.value.likeCount = res.data.data;
      hasLiked.value = !hasLiked.value;
      console.log('点赞状态:', hasLiked.value);
      console.log('点赞数:', article.value.likeCount);
    } else {
      console.error('点赞失败:', res.data.msg);
    }
  } catch (error) {
    console.error('Error toggling like:', error);
    if (error.response && error.response.data.code === '401') {
      router.push('/login');
    }
  }
}

// 加载评论
const loadComments = async (articleId) => {
  try {
    const response = await axios.get(`http://localhost:8081/comment/article/${articleId}`)
    // 只显示状态为正常的评论（status === 1）
    comments.value = response.data.data.filter(comment => comment.status === 1)
  } catch (err) {
    console.error('加载评论失败:', err)
    ElMessage.error('加载评论失败')
  }
}

// 加载更多回复
const loadMoreReplies = async (comment) => {
  try {
    if (!comment.replies) {
      comment.replies = []
    }

    const response = await axios.get(`http://localhost:8081/comment/replies/${comment.id}`)
    // 只显示状态为正常的回复
    const validReplies = response.data.data.filter(reply => reply.status === 1)
    comment.replies = [...(comment.replies || []), ...validReplies]
  } catch (err) {
    console.error('加载回复失败:', err)
    ElMessage.error('加载回复失败')
  }
}

// 提交新评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await axios.post(`http://localhost:8081/comment/add?userId=${localStorage.getItem('userId')}`, {
      articleId: article.value.id,
      content: newComment.value,
      parentId: null
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.data.code === '0') {
      ElMessage.success('评论发表成功，等待审核')
      newComment.value = ''
      await loadComments(article.value.id)
    } else {
      ElMessage.error(response.data.msg || '发表评论失败')
    }
  } catch (err) {
    console.error('发表评论失败:', err)
    if (err.response?.status === 401) {
      ElMessage.warning('请先登录')
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.msg || '发表评论失败')
    }
  }
}

// 提交回复
const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await axios.post(`http://localhost:8081/comment/add?userId=${localStorage.getItem('userId')}`, {
      articleId: article.value.id,
      content: replyContent.value,
      parentId: parentId
    }, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (response.data.code === '0') {
      ElMessage.success('回复发表成功，等待审核')
      const newReply = response.data.data
      const parentComment = comments.value.find(c => c.id === parentId)
      if (parentComment) {
        parentComment.replies = [...(parentComment.replies || []), newReply]
      }
      replyContent.value = ''
      activeReplyId.value = null
    } else {
      ElMessage.error(response.data.msg || '回复失败')
    }
  } catch (err) {
    console.error('回复失败:', err)
    if (err.response?.status === 401) {
      ElMessage.warning('请先登录')
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.msg || '回复失败')
    }
  }
}

// 切换回复框显示
const toggleReplyBox = (commentId) => {
  activeReplyId.value = activeReplyId.value === commentId ? null : commentId
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  activeReplyId.value = null
  replyContent.value = ''
}

// 格式化日期
const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.article-detail.wide {
  max-width: 1200px;
}

.article-detail.centered {
  max-width: 700px;
}

.article-meta {
  color: #888;
  margin-bottom: 1rem;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
}

.status-tag.private {
  background-color: #e6a23c;
  color: white;
}

.status-tag.top {
  background-color: #409eff;
  color: white;
}

.content {
  margin-bottom: 2rem;
}

.content.small {
  font-size: 14px;
}

.content.medium {
  font-size: 16px;
}

.content.large {
  font-size: 18px;
}

.article-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  padding: 15px;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.author-actions {
  display: flex;
  gap: 12px;
}

.user-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-button .count {
  margin-left: 4px;
  font-size: 14px;
  color: #909399;
}

.settings-form {
  padding: 20px;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 16px;
  color: #909399;
}

.comments-section {
  margin-top: 40px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.comment-input textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: none;
  margin-bottom: 10px;
}

.comment-input button {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: bold;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin: 8px 0;
  line-height: 1.5;
}

.action-btn {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  padding: 4px 0;
  font-size: 14px;
}

.reply-box {
  margin: 10px 0 10px 40px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.reply-box textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 8px;
  resize: vertical;
}

.reply-actions {
  display: flex;
  gap: 8px;
}

.reply-actions button {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.reply-actions button:first-child {
  background-color: #409eff;
  color: white;
}

.reply-actions button:last-child {
  background-color: #f56c6c;
  color: white;
}

.replies {
  margin-left: 40px;
  margin-top: 10px;
  border-left: 2px solid #eee;
  padding-left: 10px;
}

.reply-item {
  padding: 10px 0;
}

.load-more-btn {
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  padding: 4px 0;
  font-size: 14px;
  margin-top: 5px;
}

.load-more-btn:hover {
  text-decoration: underline;
}

.el-button .el-icon {
  margin-right: 4px;
  vertical-align: middle;
}
</style>