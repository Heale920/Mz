  <template>
    <div class="article-management">
      <div class="page-header">
        <h1>文章管理</h1>
        <div class="header-actions">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索文章标题"
              class="search-input"
              @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <i class="el-icon-search"></i>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 文章列表 -->
      <el-table
          :data="articles"
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column
            prop="title"
            label="标题"
            min-width="200"
        >
          <template #default="scope" >
            <el-link type="primary" @click="viewArticle(scope.row)">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column
            prop="authorName"
            label="作者"
            width="120"
        />

        <el-table-column
            prop="createTime"
            label="发布时间"
            width="180"
        >
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
            prop="status"
            label="状态"
            width="100"
        >
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
            prop="viewCount"
            label="浏览量"
            width="100"
        />

        <el-table-column
            label="操作"
            width="200"
            fixed="right"
        >
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                @click="editArticle(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </template>

  <script>
  import axios from 'axios';
  import { ElMessage, ElMessageBox } from 'element-plus';

  export default {
    name: 'ArticleManagement',
    data() {
      return {
        articles: [],
        loading: false,
        currentPage: 1,
        pageSize: 10,
        total: 0,
        searchKeyword: ''
      };
    },
    created() {
      this.fetchArticles();
    },
    methods: {
      async fetchArticles() {
        this.loading = true;
        try {
          const response = await axios.get('http://localhost:8081/admin/allArticle', {
            params: {
              page: this.currentPage,
              size: this.pageSize,
              status: this.status,
            },
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token')}`
            }
          });

          if (response.data.code === '0') {
            const data = response.data.data;
            this.articles = data.records || [];
            this.total = data.total || 0;
          } else {
            ElMessage.error(response.data.msg || '获取文章列表失败');
          }
        } catch (error) {
          console.error('获取文章列表失败:', error);
          ElMessage.error('获取文章列表失败: ' + (error.response?.data?.msg || error.message));
        } finally {
          this.loading = false;
        }
      },

      formatDate(date) {
        if (!date) return '';
        return new Date(date).toLocaleString();
      },

      getStatusType(status) {
        const types = {
          0: 'info',    // 草稿
          1: 'success', // 已发布
          2: 'warning'  // 待审核
        };
        return types[status] || 'info';
      },

      getStatusText(status) {
        const texts = {
          0: '草稿',
          1: '已发布',
          2: '待审核'
        };
        return texts[status] || '未知';
      },

      handleSearch() {
        this.currentPage = 1;
        this.fetchArticles();
      },

      handleSizeChange(val) {
        this.pageSize = val;
        this.fetchArticles();
      },

      handleCurrentChange(val) {
        this.currentPage = val;
        this.fetchArticles();
      },

      viewArticle(article) {
        this.$router.push(`/article/${article.id}`);
      },

      editArticle(article) {
        this.$router.push(`/article/edit/${article.id}`);
      },

      async handleDelete(article) {
        try {
          await ElMessageBox.confirm(
              '确定要删除这篇文章吗？此操作不可逆',
              '警告',
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }
          );

          const response = await axios.delete(`http://localhost:8081/admin/article/${article.id}`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem('token')}`
            }
          });

          if (response.data.code === '0') {
            ElMessage.success('删除成功');
            this.fetchArticles();
          } else {
            ElMessage.error(response.data.msg || '删除失败');
          }
        } catch (error) {
          if (error !== 'cancel') {
            console.error('删除文章失败:', error);
            ElMessage.error('删除文章失败: ' + (error.response?.data?.msg || error.message));
          }
        }
      }
    }
  };
  </script>

  <style scoped>
  .article-management {
    padding: 20px;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .header-actions {
    display: flex;
    gap: 16px;
  }

  .search-input {
    width: 300px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .el-tag {
    text-align: center;
    width: 65px;
  }
  </style>