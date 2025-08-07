<template>
  <div class="tag-management">
    <div class="page-header">
      <h2>标签管理</h2>
      <div class="sub-title">管理文章标签</div>
    </div>

    <!-- 添加标签 -->
    <div class="add-tag-section">
      <el-form :inline="true" class="tag-form">
        <el-form-item label="标签名称">
          <el-input
              v-model="newTagName"
              placeholder="请输入标签名称"
              @keyup.enter="addTag"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addTag" :loading="loading">
            添加标签
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 标签列表 -->
    <div class="tag-list-section">
      <el-table
          :data="tags"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column
            type="index"
            label="序号"
            width="80"
            align="center"
        />
        <el-table-column
            prop="name"
            label="标签名称"
            min-width="150"
            align="center"
        />
        <el-table-column
            label="操作"
            width="150"
            align="center"
        >
          <template #default="scope">
            <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  name: 'TagManagement',
  data() {
    return {
      tags: [],
      newTagName: '',
      loading: false
    };
  },
  created() {
    this.fetchTags();
  },
  methods: {
    // 获取所有标签
    async fetchTags() {
      this.loading = true;
      try {
        const response = await axios.get('http://localhost:8081/tag/TagList', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        if (response.data.code === '0') {
          this.tags = response.data.data;
        } else {
          ElMessage.error(response.data.msg || '获取标签列表失败');
        }
      } catch (error) {
        console.error('获取标签失败:', error);
        ElMessage.error('获取标签列表失败，请检查网络连接');
      } finally {
        this.loading = false;
      }
    },

    // 添加标签
    async addTag() {
      if (!this.newTagName.trim()) {
        ElMessage.warning('请输入标签名称');
        return;
      }

      this.loading = true;
      try {
        const response = await axios.post('http://localhost:8081/tag/add',
            { name: this.newTagName.trim() },
            {
              headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
              }
            }
        );

        if (response.data.code === '0') {
          ElMessage.success('添加标签成功');
          this.newTagName = '';
          await this.fetchTags();
        } else {
          ElMessage.error(response.data.msg || '添加标签失败');
        }
      } catch (error) {
        console.error('添加标签失败:', error);
        ElMessage.error(error.response?.data?.msg || '添加标签失败，请重试');
      } finally {
        this.loading = false;
      }
    },

    // 删除标签
    async handleDelete(tag) {
      try {
        await ElMessageBox.confirm(
            `确定要删除标签"${tag.name}"吗？`,
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        );

        this.loading = true;
        const response = await axios.delete(`http://localhost:8081/tag/${tag.id}`, {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });

        if (response.data.code === '0') {
          ElMessage.success('删除标签成功');
          await this.fetchTags();
        } else {
          ElMessage.error(response.data.msg || '删除标签失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除标签失败:', error);
          ElMessage.error('删除标签失败，请重试');
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.tag-management {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.sub-title {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.add-tag-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.tag-form {
  display: flex;
  align-items: center;
}

.tag-list-section {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .tag-form {
    flex-direction: column;
    align-items: stretch;
  }

  .tag-form .el-form-item {
    margin-right: 0;
    margin-bottom: 15px;
  }
}
</style>