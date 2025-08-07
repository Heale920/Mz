<template>
  <div class="admin-dashboard">
    <!-- 左侧菜单栏 -->
    <div class="sidebar">
      <div class="system-title">博客信息管理系统</div>
      <div class="menu-list">
        <div class="menu-title">功能菜单</div>
        <router-link to="/dashboard" class="menu-item active">首页概览</router-link>
        <router-link to="/management" class="menu-item">用户管理</router-link>
        <router-link to="/management1" class="menu-item">文章管理</router-link>
        <router-link to="/management2" class="menu-item">评论管理</router-link>
        <router-link to="/tag" class="menu-item">标签管理</router-link>
        <router-link to="/statistics" class="menu-item">数据统计</router-link>
        <router-link to="/settings" class="menu-item">系统设置</router-link>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-content">
      <router-view />
      <div class="page-header">
        <h1>后台管理首页</h1>
        <div class="sub-title">欢迎使用博客信息管理系统</div>
      </div>

      <div class="welcome-card">
        <div class="smile-icon">😊</div>
        <h2>欢迎, {{ currentAdmin }}</h2>
        <div class="user-info">
          您已成功登录博客管理系统
          <div class="info-detail">
            当前角色：管理员 | 登录时间：{{ loginTime }}
          </div>
        </div>
      </div>

      <div class="feature-cards">
        <div class="feature-card" @click="gotoAdminManagement">
          <div class="card-icon">👤</div>
          <div class="card-title">管理员信息</div>
          <div class="card-desc">系统管理员数据</div>
          <div class="card-count">总管理员数量：{{ totalAdmins }}</div>
        </div>
        <div class="feature-card" @click="gotoTodayArticle">
          <div class="card-icon">📝</div>
          <div class="card-title">文章统计</div>
          <div class="card-desc">文章发布数据</div>
          <div class="card-count">今日发布：{{ todayPosts }}</div>
        </div>
        <div class="feature-card" @click="gotoTodayComment">
          <div class="card-icon">💬</div>
          <div class="card-title">互动统计</div>
          <div class="card-desc">用户互动数据</div>
          <div class="card-count">今日评论：{{ todayComments }}</div>
        </div>
        <div class="feature-card warning" v-if="pendingItems > 0" @click="gotoPendingItems">
          <div class="card-icon">⚠️</div>
          <div class="card-title">待处理事项</div>
          <div class="card-desc">需要您处理的事项</div>
          <div class="card-count warning-text">待处理：{{ pendingItems }}</div>
        </div>
      </div>

      <!-- 管理员日志表格 -->
      <div class="system-log-section">
        <div class="section-header">
          <h3>管理员操作日志</h3>
          <el-button type="primary" size="small" @click="fetchAdminLogs">刷新日志</el-button>
        </div>

        <!-- 日志筛选器 -->
        <div class="filter-section">
          <el-form :inline="true" class="log-filter-form">
            <el-form-item label="管理员">
              <el-input
                  v-model="logFilter.adminName"
                  placeholder="管理员名称"
                  clearable
                  @clear="fetchAdminLogs"
                  @keyup.enter="fetchAdminLogs"
              />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select
                  v-model="logFilter.action"
                  placeholder="选择操作类型"
                  clearable
                  @change="fetchAdminLogs"
              >
                <el-option label="登录" value="登录" />
                <el-option label="更新用户状态" value="更新用户状态" />
                <el-option label="更新用户角色" value="更新用户角色" />
                <el-option label="删除用户" value="删除用户" />
                <el-option label="通过评论" value="通过评论" />
                <el-option label="驳回评论" value="驳回评论" />
                <el-option label="删除评论" value="删除评论" />
                <el-option label="回复评论" value="回复评论" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                  v-model="logFilter.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="fetchAdminLogs"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="resetFilter">重置筛选</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 日志表格 -->
        <el-table
            v-loading="logLoading"
            :data="adminLogs"
            border
            stripe
            style="width: 100%; margin-top: 20px"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
          <el-table-column prop="adminName" label="管理员" min-width="120" align="center">
            <template #default="scope">
              <el-tag size="small" type="info">{{ scope.row.adminName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="adminActionType" label="操作类型" min-width="120" align="center">
            <template #default="scope">
              <el-tag
                  size="small"
                  :type="getActionTypeTag(scope.row.adminActionType)"
              >
                {{ scope.row.adminActionType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="detail" label="操作详情" min-width="300" align="left" show-overflow-tooltip />
          <el-table-column prop="createTime" label="操作时间" min-width="180" align="center">
            <template #default="scope">
              <span>{{ formatDate(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页器 -->
        <div class="pagination-container">
          <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import router from "@/router";
import axios from 'axios';
import { ElMessage } from 'element-plus';

export default {
  data() {
    return {
      currentAdmin: '',
      loginTime: '',
      totalAdmins: 0,
      todayPosts: 0,
      todayComments: 0,
      pendingItems: 0,
      logFilter: {
        adminName: '',
        action: '',
        dateRange: []
      },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      adminLogs: [],
      logLoading: false,
      refreshInterval: null, // 用于存储自动刷新定时器
    };
  },
  created() {
    const token = localStorage.getItem('token');
    const userRole = localStorage.getItem('userRole');
    
    if (!token || userRole !== 'admin') {
      // 如果没有token或不是管理员，重定向到登录页
      this.$router.push('/login');
      return;
    }
    
    this.currentAdmin = localStorage.getItem("username") || '管理员';
    this.loginTime = new Date().toLocaleString();
    // 先获取统计数据
    this.fetchDashboardStats();
    // 立即获取日志数据，不需要用户手动点击查询
    this.fetchAdminLogs();
    // 启动自动刷新
    this.startAutoRefresh();
  },
  methods: {
    logout() {
      localStorage.clear();
      router.push('/login');
    },
    gotoArticleManagement() {
      router.push('/management1');
    },
    gotoTodayArticle() {
      router.push('/todayArticle');
    },
    gotoTodayComment() {
      router.push('/todayComment');
    },
    gotoAdminManagement() {
      router.push({ path: '/management', query: { role: 'admin' } });
    },
    gotoPendingItems() {
      this.$router.push('/pending-items');
    },
    // 获取操作类型对应的标签类型
    getActionTypeTag(actionType) {
      const actionMap = {
        '登录': 'success',
        '更新用户状态': 'warning',
        '更新用户角色': 'warning',
        '删除用户': 'danger',
        '通过评论': 'success',
        '驳回评论': 'warning',
        '删除评论': 'danger',
        '回复评论': 'info'
      };
      return actionMap[actionType] || 'info';
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      });
    },

    // 重置筛选条件并重新获取所有日志
    resetFilter() {
      this.logFilter = {
        adminName: '',
        action: '',
        dateRange: []
      };
      this.pagination.currentPage = 1;
      this.fetchAdminLogs();
    },

    // 启动自动刷新
    startAutoRefresh() {
      // 清除可能存在的旧定时器
      this.stopAutoRefresh();
      // 设置新的定时器，每30秒刷新一次
      this.refreshInterval = setInterval(() => {
        this.fetchAdminLogs();
      }, 30000);
    },

    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval);
        this.refreshInterval = null;
      }
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.fetchAdminLogs();
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.fetchAdminLogs();
    },

    async fetchDashboardStats() {
      try {
        const response = await axios.get('http://localhost:8081/admin/stats', {
          headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });

        if (response.data.code === '0') {
          const stats = response.data.data;
          this.totalAdmins = stats.adminCount || 0;
          this.todayPosts = stats.todayArticleCount || 0;
          this.todayComments = stats.todayCommentCount || 0;
        } else {
          ElMessage.error(response.data.msg || '获取统计数据失败');
        }
      } catch (error) {
        ElMessage.error('获取统计数据失败: ' + (error.response?.data?.msg || error.message));
      }
    },
    async fetchAdminLogs() {
      this.logLoading = true;
      try {
        const response = await axios.get('http://localhost:8081/admin/logs', {
          params: {
            page: this.pagination.currentPage,
            size: this.pagination.pageSize
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.data.code === '0') {
          this.adminLogs = response.data.data.records || [];
          this.pagination.total = response.data.data.total || 0;
        } else {
          ElMessage.error(response.data.msg || '获取日志失败');
        }
      } catch (error) {
        console.error('日志获取失败:', error);
        ElMessage.error('获取日志失败，请检查网络连接或重新登录');
      } finally {
        this.logLoading = false;
      }
    },
  },
  beforeUnmount() {
    // 组件销毁时停止自动刷新
    this.stopAutoRefresh();
  }
};
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
}

/* 左侧菜单栏样式 */
.sidebar {
  width: 220px;
  background: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  padding: 20px 0;
}

.system-title {
  font-size: 20px;
  font-weight: 700;
  color: #3f51b5;
  padding: 0 24px;
  margin-bottom: 30px;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-title {
  padding: 10px 24px;
  color: #888;
  font-size: 14px;
  margin-bottom: 12px;
  font-weight: bold;
}

.menu-item {
  padding: 12px 24px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
  border-left: 4px solid transparent;
}

.menu-item:hover,
.menu-item.active {
  background: #e8eaf6;
  color: #3f51b5;
  border-left: 4px solid #3f51b5;
}

/* 右侧内容区 */
.main-content {
  flex: 1;
  padding: 30px;
  background: #f5f7fa;
}

/* 顶部标题栏 */
.page-header h1 {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.sub-title {
  font-size: 14px;
  color: #888;
}

/* 欢迎卡片 */
.welcome-card {
  background: #ffffff;
  border-radius: 10px;
  padding: 30px;
  text-align: center;
  margin-top: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.smile-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.welcome-card h2 {
  margin: 0 0 10px;
  font-size: 24px;
  color: #333;
}

.user-info {
  color: #666;
  font-size: 15px;
}

.info-detail {
  margin-top: 8px;
  font-size: 13px;
  color: #999;
}

/* 功能卡片 */
.feature-cards {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.feature-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.card-icon {
  font-size: 36px;
  margin-bottom: 14px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.card-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.card-count {
  font-size: 14px;
  color: #409EFF;
  font-weight: bold;
}

/* 特殊卡片 */
.feature-card.warning {
  border: 1px solid #e6a23c;
  background-color: #fff7e6;
}

.warning-text {
  color: #e6a23c !important;
}

/* 日志部分样式 */
.system-log-section {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  margin-top: 30px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.filter-section {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
}

.log-filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-start;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 10px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-dashboard {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .feature-cards {
    flex-direction: column;
  }

  .log-filter-form {
    flex-direction: column;
  }

  .log-filter-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }
}
</style>