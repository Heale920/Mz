<template>
  <div class="user-management">
    <div class="page-header">
      <h2>{{ isAdminView ? '管理员' : '用户' }}管理</h2>
      <p>{{ isAdminView ? '查看和管理系统管理员' : '查看和管理所有注册用户' }}</p>
    </div>

    <div class="search-bar">
      <el-input
          v-model="searchKeyword"
          :placeholder="isAdminView ? '搜索管理员用户名或邮箱' : '搜索用户名或邮箱'"
          prefix-icon="el-icon-search"
          @input="handleSearch"
          class="search-input"
      />
      <el-button
          v-if="!isAdminView"
          type="primary"
          @click="toggleView"
      >
        查看管理员列表
      </el-button>
      <el-button
          v-else
          type="info"
          @click="toggleView"
      >
        查看所有用户
      </el-button>
    </div>

    <div class="user-table">
      <el-table
          v-loading="loading"
          :data="filteredUsers"
          border
          style="width: 100%"
      >
        <el-table-column prop="id" label="用户ID" min-width="100" align="center" />
        <el-table-column prop="username" label="用户名" min-width="150" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="220" align="center" />
        <el-table-column prop="role" label="角色" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'success'">
              {{ scope.row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="300" align="center">
          <template #default="scope">
            <el-button
                size="small"
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
                size="small"
                :type="scope.row.role === 'admin' ? 'warning' : 'success'"
                @click="handleRoleChange(scope.row)"
            >
              {{ scope.row.role === 'admin' ? '设置为普通用户' : '设置为管理员' }}
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

      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

export default {
  name: 'UserManagement',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const users = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const searchKeyword = ref('')
    const isAdminView = computed(() => route.query.role === 'admin')

    const filteredUsers = computed(() => {
      let filtered = users.value
      if (isAdminView.value) {
        // 管理员视图：只显示管理员
        filtered = filtered.filter(user => user.role === 'admin')
      } else {
        // 普通视图：只显示普通用户
        filtered = filtered.filter(user => user.role !== 'admin')
      }
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filtered = filtered.filter(user =>
            user.username.toLowerCase().includes(keyword) ||
            user.email.toLowerCase().includes(keyword)
        )
      }
      return filtered
    })

    const toggleView = () => {
      if (isAdminView.value) {
        router.push({ query: {} })
      } else {
        router.push({ query: { role: 'admin' } })
      }
    }

    const fetchUsers = async () => {
      loading.value = true
      try {
        const response = await axios.get('http://localhost:8081/admin/allUser', {
          params: {
            page: currentPage.value,
            size: pageSize.value,
            role: isAdminView.value ? 'admin' : undefined
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (response.data.code === '0') {
          users.value = response.data.data.records
          total.value = response.data.data.total
        } else {
          ElMessage.error(response.data.msg || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleStatusChange = async (user) => {
      try {
        const newStatus = user.status === 1 ? 0 : 1
        const response = await axios.put(`http://localhost:8081/admin/status/${user.id}`,
            { status: newStatus },
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
              }
            }
        )

        if (response.data.code === '0') {
          user.status = newStatus
          ElMessage.success(`用户已${newStatus === 1 ? '启用' : '禁用'}`)
        } else {
          ElMessage.error(response.data.msg || '更新状态失败')
        }
      } catch (error) {
        console.error('更新用户状态失败:', error)
        if (error.response) {
          ElMessage.error(`更新失败: ${error.response.data.msg || '服务器错误'}`)
        } else {
          ElMessage.error('网络错误，请稍后重试')
        }
      }
    }

    const handleRoleChange = async (user) => {
      try {
        const newRole = user.role === 'admin' ? 'user' : 'admin'
        const confirmText = newRole === 'admin' ? '确定要将该用户设置为管理员吗？' : '确定要将该管理员设置为普通用户吗？'

        await ElMessageBox.confirm(
            confirmText,
            '角色变更确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        const response = await axios.post(
            `http://localhost:8081/admin/role/${user.id}`,
            { role: newRole },  // 只发送新的角色信息
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
              }
            }
        )

        if (response.data.code === '0') {
          user.role = newRole
          ElMessage.success(`用户角色已更新为${newRole === 'admin' ? '管理员' : '普通用户'}`)
          fetchUsers()
        } else {
          ElMessage.error(response.data.msg || '更新角色失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('更新角色失败:', error)
          ElMessage.error('更新角色失败')
        }
      }
    }

    const handleDelete = async (user) => {
      try {
        await ElMessageBox.confirm(
            '确定要删除该用户吗？此操作不可逆',
            '警告',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        const response = await axios.delete(
            `http://localhost:8081/admin/delUser/${user.id}`,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
              }
            }
        )

        if (response.data.code === '0') {
          ElMessage.success('用户已删除')
          fetchUsers()
        } else {
          ElMessage.error(response.data.msg || '删除用户失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除用户失败:', error)
          ElMessage.error('删除用户失败')
        }
      }
    }

    const handleSearch = async () => {
      if (!searchKeyword.value) {
        fetchUsers()
        return
      }

      loading.value = true
      try {
        const response = await axios.get("http://localhost:8081/admin/search", {
          params: {
            keyword: searchKeyword.value,
            page: currentPage.value,
            size: pageSize.value
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (response.data.code === '0') {
          users.value = response.data.data.records
          total.value = response.data.data.total
          ElMessage.success('查询成功！')
        } else {
          ElMessage.error(response.data.msg || '查询失败！')
        }
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error(error.response?.data?.msg || '搜索失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchUsers()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchUsers()
    }

    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    // 监听路由参数变化
    watch(() => route.query.role, () => {
      fetchUsers()
    })

    onMounted(() => {
      fetchUsers()
    })

    return {
      loading,
      users,
      filteredUsers,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      fetchUsers,
      handleRoleChange,
      handleDelete,
      handleStatusChange,
      handleSizeChange,
      handleCurrentChange,
      handleSearch,
      formatDate,
      toggleView,
      isAdminView
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 24px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  min-height: calc(100vh - 48px);
  transition: all 0.3s ease;
}

.page-header {
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #1a1a1a;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.page-header p {
  margin: 8px 0 0;
  font-size: 15px;
  color: #666;
  line-height: 1.5;
}

.search-bar {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
}

.search-input {
  width: 360px;
  transition: all 0.3s ease;
}

.search-input:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-input:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-input:deep(.el-input__inner) {
  font-size: 14px;
}

.user-table {
  margin-top: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.user-table:deep(.el-table) {
  width: 100%;
  table-layout: fixed;
}

.user-table:deep(.el-table th) {
  background-color: #fafafa !important;
  color: #1a1a1a;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 0;
  border-bottom: 2px solid #f0f0f0;
}

.user-table:deep(.el-table td) {
  padding: 16px 8px;
  color: #333;
}

.user-table:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: #f8f9ff;
}

.user-table:deep(.el-table__row) {
  transition: all 0.3s ease;
}

.pagination {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

.pagination:deep(.el-pagination) {
  justify-content: flex-end;
}

.pagination:deep(.el-pagination .el-pagination__total) {
  font-size: 14px;
}

.pagination:deep(.el-pagination .el-pagination__sizes) {
  margin-left: 16px;
}

.el-button {
  margin-left: 12px;
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-button:first-child {
  margin-left: 0;
}

.el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.el-tag {
  min-width: 72px;
  text-align: center;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: 500;
}

/* 状态标签样式 */
.el-tag.el-tag--success {
  background-color: #e6f7e6;
  border-color: #c2e7c2;
  color: #18a058;
}

.el-tag.el-tag--danger {
  background-color: #ffeded;
  border-color: #ffdbdb;
  color: #d03050;
}

/* 操作按钮样式优化 */
.el-button--success {
  background-color: #18a058;
  border-color: #18a058;
}

.el-button--success:hover {
  background-color: #0a904d;
  border-color: #0a904d;
}

.el-button--warning {
  background-color: #f0a020;
  border-color: #f0a020;
}

.el-button--warning:hover {
  background-color: #e09010;
  border-color: #e09010;
}

.el-button--danger {
  background-color: #d03050;
  border-color: #d03050;
}

.el-button--danger:hover {
  background-color: #c02040;
  border-color: #c02040;
}

/* 表格内容居中对齐 */
.user-table:deep(.el-table .cell) {
  padding: 0 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-table:deep(.el-table__header) {
  width: 100% !important;
}

.user-table:deep(.el-table__body) {
  width: 100% !important;
}

/* 空状态样式优化 */
.user-table:deep(.el-table__empty-block) {
  min-height: 300px;
  background-color: #fafafa;
}

.user-table:deep(.el-table__empty-text) {
  color: #999;
  font-size: 14px;
}

/* 确保表格在小屏幕上也能保持良好的布局 */
@media screen and (max-width: 1400px) {
  .user-table:deep(.el-table .cell) {
    padding: 0 8px;
  }

  .el-button + .el-button {
    margin-left: 8px;
  }
}
</style>