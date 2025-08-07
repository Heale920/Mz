<template>
  <div class="pending-items">
    <div class="page-header">
      <h1>待处理事项</h1>
      <div class="sub-title">需要管理员处理的事项</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stats-card">
        <template #header>
          <div class="card-header">
            <span>待审核评论</span>
            <el-tag type="danger" v-if="pendingComments.length > 0">{{ pendingComments.length }}</el-tag>
          </div>
        </template>
        <div class="card-content">
          <div v-if="pendingComments.length === 0" class="empty-placeholder">
            暂无待审核评论
          </div>
          <div v-else class="comment-list">
            <div v-for="comment in pendingComments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="username">{{ comment.username }}</span>
                <span class="time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-article">
                文章：{{ comment.articleTitle }}
              </div>
              <div class="comment-actions">
                <el-button type="success" size="small" @click="approveComment(comment.id)">通过</el-button>
                <el-button type="danger" size="small" @click="rejectComment(comment.id)">拒绝</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="stats-card">
        <template #header>
          <div class="card-header">
            <span>待发布文章</span>
            <el-tag type="warning" v-if="draftArticles.length > 0">{{ draftArticles.length }}</el-tag>
          </div>
        </template>
        <div class="card-content">
          <div v-if="draftArticles.length === 0" class="empty-placeholder">
            暂无待发布文章
          </div>
          <div v-else class="article-list">
            <div v-for="article in draftArticles" :key="article.id" class="article-item">
              <div class="article-title">{{ article.title }}</div>
              <div class="article-info">
                <span>作者：{{ article.author }}</span>
                <span>创建时间：{{ article.createTime }}</span>
              </div>
              <div class="article-actions">
                <el-button type="primary" size="small" @click="viewArticle(article.id)">查看</el-button>
                <el-button type="success" size="small" @click="publishArticle(article.id)">发布</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
  name: 'PendingItems',
  data() {
    return {
      pendingComments: [],
      draftArticles: []
    };
  },
  created() {
    this.fetchPendingComments();
    this.fetchDraftArticles();
  },
  methods: {
    async fetchPendingComments() {
      try {
        const response = await axios.get('http://localhost:8081/admin/comments/pending', {
          headers: {
            Authorization: `Bearer \${localStorage.getItem('token')}`
          }
        });
        if (response.data.code === '0') {
          this.pendingComments = response.data.data.map(comment => ({
            ...comment,
            createTime: new Date(comment.createTime).toLocaleString()
          }));
        } else {
          ElMessage.error(response.data.msg || '获取待审核评论失败');
        }
      } catch (error) {
        ElMessage.error('获取待审核评论失败: ' + error.message);
      }
    },
    async fetchDraftArticles() {
      try {
        const response = await axios.get('http://localhost:8081/admin/articles/drafts', {
          headers: {
            Authorization: `Bearer \${localStorage.getItem('token')}`
          }
        });
        if (response.data.code === '0') {
          this.draftArticles = response.data.data.map(article => ({
            ...article,
            createTime: new Date(article.createTime).toLocaleString()
          }));
        } else {
          ElMessage.error(response.data.msg || '获取待发布文章失败');
        }
      } catch (error) {
        ElMessage.error('获取待发布文章失败: ' + error.message);
      }
    },
    async approveComment(commentId) {
      try {
        const response = await axios.post(`http://localhost:8081/admin/comments/${commentId}/approve`, null, {
          headers: {
            Authorization: `Bearer \${localStorage.getItem('token')}`
          }
        });
        if (response.data.code === '0') {
          ElMessage.success('评论审核通过');
          this.fetchPendingComments();
        } else {
          ElMessage.error(response.data.msg || '操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败: ' + error.message);
      }
    },
    async rejectComment(commentId) {
      try {
        const response = await axios.post(`http://localhost:8081/admin/comments/${commentId}/reject`, null, {
          headers: {
            Authorization: `Bearer \${localStorage.getItem('token')}`
          }
        });
        if (response.data.code === '0') {
          ElMessage.success('评论已拒绝');
          this.fetchPendingComments();
        } else {
          ElMessage.error(response.data.msg || '操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败: ' + error.message);
      }
    },
    viewArticle(articleId) {
      this.$router.push(`/article/${articleId}`);
    },
    async publishArticle(articleId) {
      try {
        const response = await axios.post(`http://localhost:8081/admin/articles/${articleId}/publish`, null, {
          headers: {
            Authorization: `Bearer \${localStorage.getItem('token')}`
          }
        });
        if (response.data.code === '0') {
          ElMessage.success('文章发布成功');
          this.fetchDraftArticles();
        } else {
          ElMessage.error(response.data.msg || '发布失败');
        }
      } catch (error) {
        ElMessage.error('发布失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.pending-items {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.sub-title {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stats-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.empty-placeholder {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.comment-list, .article-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item, .article-item {
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.comment-item:last-child, .article-item:last-child {
  border-bottom: none;
}

.comment-header, .article-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.comment-content, .article-title {
  margin-bottom: 8px;
  color: #303133;
}

.comment-article {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.comment-actions, .article-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.username {
  font-weight: bold;
  color: #409EFF;
}

.time {
  color: #909399;
}
</style>