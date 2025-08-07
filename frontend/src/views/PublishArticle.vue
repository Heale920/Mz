<template>
  <div class="publish-container">
    <h2>发布新文章</h2>

    <div class="form-group">
      <label>标题</label>
      <input v-model="article.title" type="text" class="input" placeholder="请输入标题" />
    </div>

    <div class="form-group">
      <label>标签</label>
      <div class="tag-list">
        <label v-for="tag in allTags" :key="tag.id" class="tag-item">
          <input type="checkbox" :value="tag.id" v-model="selectedTags" />
          {{ tag.name }}
        </label>
      </div>
    </div>

    <div class="form-group">
      <label>内容</label>
      <textarea v-model="article.content" rows="10" class="textarea" placeholder="请输入文章内容"></textarea>
    </div>

    <button @click="submitArticle" class="publish-btn" :disabled="isSubmitting">
      {{ isSubmitting ? '发布中...' : '发布' }}
    </button>
  </div>
</template>

<script>
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';

export default {
  setup() {
    const router = useRouter();
    return { router };
  },
  data() {
    return {
      article: {
        title: '',
        content: '',
        status: 1, // 1-已发布
        visibility: 'PUBLIC', // 默认公开
        isTop: false, // 默认不置顶
        isRecommend: false, // 默认不推荐
        viewCount: 0,
        likeCount: 0,
        favoriteCount: 0,
        commentCount: 0
      },
      allTags: [],
      selectedTags: [],
      isSubmitting: false
    };
  },
  created() {
    this.getTags();
  },
  methods: {
    async getTags() {
      try {
        const token = localStorage.getItem('token');
        const res = await axios.get('http://localhost:8081/tag/TagList', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        if (res.data.code === '0') {
          this.allTags = res.data.data;
        } else {
          ElMessage.error('标签加载失败: ' + (res.data.msg || '未知错误'));
        }
      } catch (error) {
        console.error('标签请求失败:', error);
        ElMessage.error('标签加载失败，请刷新重试');
      }
    },
    async submitArticle() {
      // 表单验证
      if (!this.article.title.trim()) {
        ElMessage.warning('请输入标题');
        return;
      }
      if (!this.article.content.trim()) {
        ElMessage.warning('请输入内容');
        return;
      }
      if (this.selectedTags.length === 0) {
        ElMessage.warning('请至少选择一个标签');
        return;
      }

      this.isSubmitting = true;

      try {
        const token = localStorage.getItem('token');

        // 使用正确的日期格式
        const now = dayjs().format('YYYY-MM-DD HH:mm:ss');

        const dataToSend = {
          ...this.article,
          tagList: this.selectedTags,
          createTime: now,
          updateTime: now
        };

        console.log('准备发送的数据:', dataToSend);

        const res = await axios.post('http://localhost:8081/article/publish', dataToSend, {
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
          }
        });

        if (res.data.code === '0') {
          ElMessage.success('发布成功');
          this.router.push('/dashboard1');
        } else {
          ElMessage.error(res.data.msg || '发布失败');
        }
      } catch (error) {
        console.error('发布失败:', error);
        if (error.response) {
          if (error.response.status === 401) {
            ElMessage.error('登录已过期，请重新登录');
            this.router.push('/login');
          } else {
            ElMessage.error(error.response.data.msg || '发布失败');
          }
        } else {
          ElMessage.error('网络错误，请稍后重试');
        }
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.input, .textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.3s;
}

.input:focus, .textarea:focus {
  border-color: #1890ff;
  outline: none;
}

.textarea {
  min-height: 200px;
  resize: vertical;
  font-family: inherit;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background: #f0f0f0;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.tag-item:hover {
  background: #e0e0e0;
}

.tag-item input[type="checkbox"] {
  margin-right: 5px;
}

.publish-btn {
  display: block;
  width: 100%;
  max-width: 200px;
  margin: 30px auto 0;
  background-color: #1890ff;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
}

.publish-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
}

.publish-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
}
</style>