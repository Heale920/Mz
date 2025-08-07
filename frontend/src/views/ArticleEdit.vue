<template>
  <div class="article-edit">
    <div class="edit-container">
      <h2>{{ isEdit ? '编辑文章' : '发布文章' }}</h2>

      <el-form :model="articleForm" :rules="rules" ref="articleFormRef" label-width="80px">
        <!-- 标题 -->
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="articleForm.title"
              placeholder="请输入文章标题"
              maxlength="100"
              show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 标签 -->
        <el-form-item label="标签" prop="tags">
          <el-select
              v-model="articleForm.tags"
              multiple
              placeholder="请选择文章标签"
              style="width: 100%"
          >
            <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <!-- Markdown编辑器 -->
        <el-form-item label="内容" prop="content">
          <div class="editor-container">
            <div class="markdown-editor">
              <el-tabs v-model="activeTab" type="border-card">
                <el-tab-pane label="编辑" name="edit">
                  <el-input
                      v-model="articleForm.content"
                      type="textarea"
                      :rows="15"
                      placeholder="请输入文章内容（支持Markdown格式）"
                  ></el-input>
                </el-tab-pane>
                <el-tab-pane label="预览" name="preview">
                  <div class="markdown-preview" v-html="compiledMarkdown"></div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '发布文章' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()

// 表单引用
const articleFormRef = ref(null)

// 表单数据
const articleForm = ref({
  title: '',
  content: '',
  tags: []
})

// 编辑器相关
const activeTab = ref('edit')
const submitting = ref(false)
const isEdit = ref(false)
const tags = ref([])

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 10, message: '文章内容不能少于10个字符', trigger: 'blur' }
  ]
}

// 计算属性：将Markdown转换为HTML
const compiledMarkdown = computed(() => {
  const rawHtml = marked(articleForm.value.content || '')
  return DOMPurify.sanitize(rawHtml)
})

// 初始化加载
onMounted(async () => {
  // 获取所有标签
  await fetchTags()

  // 如果是编辑模式，获取文章详情
  const articleId = route.params.id
  if (articleId) {
    isEdit.value = true
    await fetchArticleDetail(articleId)
  }
})

// 获取所有标签
const fetchTags = async () => {
  try {
    const response = await axios.get('http://localhost:8081/tag/TagList', {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })
    if (response.data.code === "0") {
      tags.value = response.data.data
    }
  } catch (error) {
    console.error('获取标签失败:', error)
    ElMessage.error('获取标签列表失败')
  }
}

// 获取文章详情
const fetchArticleDetail = async (id) => {
  try {
    const response = await axios.get(`http://localhost:8081/article/${id}`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })

    if (response.data.code === "0") {
      const article = response.data.data
      articleForm.value = {
        title: article.title,
        content: article.content,
        tags: article.tagList || []
      }
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
    router.push('/') // 获取失败时返回首页
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!articleFormRef.value) return

  try {
    await articleFormRef.value.validate()

    submitting.value = true
    const articleId = route.params.id
    const url = isEdit.value
        ? `http://localhost:8081/article/${articleId}`
        : 'http://localhost:8081/article/publish'

    const method = isEdit.value ? 'put' : 'post'

    // 构造提交数据，确保字段名匹配
    const submitData = {
      title: articleForm.value.title,
      content: articleForm.value.content,
      tagList: articleForm.value.tags, // 标签列表
      status: 1, // 1-已发布
      visibility: 'PUBLIC', // 默认公开
      isTop: false, // 默认不置顶
      isRecommend: false, // 默认不推荐
      viewCount: 0,
      likeCount: 0,
      favoriteCount: 0,
      commentCount: 0
    }

    const response = await axios({
      method,
      url,
      data: submitData,
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`
      }
    })

    if (response.data.code === "0") {
      ElMessage.success(isEdit.value ? '文章更新成功' : '文章发布成功')
      router.push(`/article/${isEdit.value ? articleId : response.data.data.id}`)
    } else {
      throw new Error(response.data.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  ElMessage.info('已取消编辑')
  router.back()
}
</script>

<style scoped>
.article-edit {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.edit-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.edit-container h2 {
  margin-bottom: 30px;
  text-align: center;
  color: #333;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.markdown-editor {
  width: 100%;
}

.markdown-preview {
  padding: 15px;
  min-height: 300px;
  max-height: 600px;
  overflow-y: auto;
  background: #fafafa;
}

/* Markdown 预览样式 */
.markdown-preview :deep(h1) {
  font-size: 2em;
  margin: 0.67em 0;
}

.markdown-preview :deep(h2) {
  font-size: 1.5em;
  margin: 0.75em 0;
}

.markdown-preview :deep(h3) {
  font-size: 1.17em;
  margin: 0.83em 0;
}

.markdown-preview :deep(p) {
  margin: 1em 0;
}

.markdown-preview :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.markdown-preview :deep(pre) {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}

.markdown-preview :deep(blockquote) {
  margin: 1em 0;
  padding-left: 1em;
  border-left: 4px solid #ddd;
  color: #666;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 2em;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  height: auto;
}

/* 表单样式调整 */
:deep(.el-form-item__label) {
  font-weight: bold;
}

:deep(.el-input__wrapper) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace;
}
</style>