<template>
  <div class="admin-dashboard">
    <!-- 左侧菜单栏 -->
    <div class="sidebar">
      <div class="system-title">
        <i class="el-icon-eleme-filled"></i>
        <span>博客管理系统</span>
      </div>
      <div class="menu-list">
        <div class="menu-group-title">导航菜单</div>
        <router-link to="/dashboard" class="menu-item active">
          <i class="el-icon-monitor"></i>
          <span>首页概览</span>
        </router-link>
        <router-link to="/management" class="menu-item">
          <i class="el-icon-user"></i>
          <span>用户管理</span>
        </router-link>
        <router-link to="/management1" class="menu-item">
          <i class="el-icon-document"></i>
          <span>文章管理</span>
        </router-link>
        <router-link to="/management2" class="menu-item">
          <i class="el-icon-chat-dot-round"></i>
          <span>评论管理</span>
        </router-link>
        <router-link to="/management3" class="menu-item">
          <i class="el-icon-price-tag"></i>
          <span>标签管理</span>
        </router-link>
        <router-link to="/data" class="menu-item">
          <i class="el-icon-data-line"></i>
          <span>数据统计</span>
        </router-link>
      </div>
      <div class="sidebar-footer">
        <router-link to="/settings" class="menu-item">
          <i class="el-icon-setting"></i>
          <span>系统设置</span>
        </router-link>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1>欢迎, {{ currentAdmin }}! 👋</h1>
          <div class="sub-title">今天是新的一天，开始管理你的博客吧。</div>
        </div>
        <div class="header-right">
          <el-tag type="info" size="large" effect="plain">当前角色：管理员</el-tag>
          <el-tag type="info" size="large" effect="plain">登录时间：{{ loginTime }}</el-tag>
        </div>
      </div>

      <!-- 核心数据统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card" @click="gotoAdminManagement">
          <div class="card-icon-wrapper" style="background-color: #eaf2ff;">
            <i class="el-icon-user-solid" style="color: #409eff;"></i>
          </div>
          <div class="card-info">
            <div class="card-title">系统管理员</div>
            <div class="card-count">{{ totalAdmins }}</div>
            <div class="card-desc">平台总管理员数量</div>
          </div>
        </div>
        <div class="stat-card" @click="gotoTodayArticle">
          <div class="card-icon-wrapper" style="background-color: #e9fbf5;">
            <i class="el-icon-edit" style="color: #67c23a;"></i>
          </div>
          <div class="card-info">
            <div class="card-title">今日发布</div>
            <div class="card-count">{{ todayPosts }}</div>
            <div class="card-desc">今日新增文章总数</div>
          </div>
        </div>
        <div class="stat-card" @click="gotoTodayComment">
          <div class="card-icon-wrapper" style="background-color: #fff6e9;">
            <i class="el-icon-s-comment" style="color: #e6a23c;"></i>
          </div>
          <div class="card-info">
            <div class="card-title">今日评论</div>
            <div class="card-count">{{ todayComments }}</div>
            <div class="card-desc">今日新增评论总数</div>
          </div>
        </div>
        <div class="stat-card warning" v-if="pendingItems > 0" @click="gotoPendingItems">
          <div class="card-icon-wrapper" style="background-color: #ffeeed;">
            <i class="el-icon-warning" style="color: #f56c6c;"></i>
          </div>
          <div class="card-info">
            <div class="card-title">待办事项</div>
            <div class="card-count">{{ pendingItems }}</div>
            <div class="card-desc">您有新的待处理事项</div>
          </div>
        </div>
      </div>

      <!-- 管理员日志 -->
      <div class="log-section-wrapper">
        <div class="section-header">
          <h3>管理员操作日志</h3>
          <el-button type="primary" icon="el-icon-refresh" @click="fetchAdminLogs">刷新日志</el-button>
        </div>

        <!-- 日志筛选器 -->
        <div class="filter-container">
          <el-form :inline="true" :model="logFilter" class="log-filter-form">
            <el-form-item label="管理员">
              <el-input v-model="logFilter.adminName" placeholder="输入管理员名称" clearable @keyup.enter="fetchAdminLogs" />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="logFilter.action" placeholder="请选择操作类型" clearable @change="fetchAdminLogs">
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
            <el-form-item label="操作时间">
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
              <el-button @click="resetFilter" icon="el-icon-refresh-left">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 日志表格 -->
        <el-table
            v-loading="logLoading"
            :data="adminLogs"
            stripe
            style="width: 100%;"
            :header-cell-style="{ background: '#fafafa', color: '#333', 'font-weight': 'bold' }"
        >
          <el-table-column prop="adminName" label="管理员" width="150" align="center">
            <template #default="scope">
              <el-tag size="medium">{{ scope.row.adminName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="adminActionType" label="操作类型" width="180" align="center">
            <template #default="scope">
              <el-tag size="medium" :type="getActionTypeTag(scope.row.adminActionType)" effect="light">
                {{ scope.row.adminActionType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
          <el-table-column prop="createTime" label="操作时间" width="200" align="center">
            <template #default="scope">
              <i class="el-icon-time" style="margin-right: 5px;"></i>
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
              background
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
      <router-view />
    </div>
  </div>
</template>

<style scoped>
/* 引入 Element Plus 图标 */
@import url("//unpkg.com/element-plus/dist/index.css");
@import url("https://at.alicdn.com/t/c/font_4643932_7n28ik09r7g.css");

/* 全局布局 */
.admin-dashboard {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
  'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

/* 左侧菜单栏 */
.sidebar {
  width: 240px;
  background-color: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.system-title {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-size: 20px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  border-bottom: 1px solid #002140;
}

.system-title i {
  font-size: 28px;
  margin-right: 10px;
  color: #409eff;
}

.menu-list {
  flex-grow: 1;
  overflow-y: auto;
  padding: 15px 0;
}

.menu-group-title {
  padding: 10px 20px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  text-transform: uppercase;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 25px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  transition: all 0.3s;
  font-size: 15px;
}

.menu-item i {
  margin-right: 15px;
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.menu-item:hover {
  background-color: #002140;
  color: #fff;
}

.menu-item.active,
.router-link-exact-active {
  background-color: #409eff;
  color: #fff;
  border-right: 3px solid #fff;
}

.sidebar-footer {
  padding: 15px 0;
  border-top: 1px solid #002140;
}

/* 右侧内容区 */
.main-content {
  flex-grow: 1;
  padding: 25px 30px;
  overflow-y: auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.page-header .sub-title {
  font-size: 15px;
  color: #909399;
  margin-top: 8px;
}

.header-right .el-tag {
  margin-left: 10px;
  font-size: 14px;
}


/* 数据统计卡片 */
.stats-cards {
  display: flex; /* <-- 修改点: 从 grid 改为 flex */
  flex-wrap: wrap; /* <-- 新增: 允许卡片在小屏幕上换行 */
  gap: 25px;
  margin-bottom: 30px;
}

.stat-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 25px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e6ebf5;
  flex: 1; /* <-- 新增: 让每个卡片平均分配空间 */
  min-width: 240px; /* <-- 新增: 保证卡片的最小宽度 */
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.card-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  flex-shrink: 0; /* 防止图标区域被压缩 */
}

.card-icon-wrapper i {
  font-size: 28px;
}

.card-info .card-title {
  font-size: 15px;
  color: #909399;
  margin-bottom: 8px;
}

.card-info .card-count {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}
.card-info .card-desc {
  font-size: 13px;
  color: #c0c4cc;
  margin-top: 8px;
}

.stat-card.warning {
  border-left: 5px solid #f56c6c;
}


/* 日志区域 */
.log-section-wrapper {
  background-color: #fff;
  padding: 25px;
  border-radius: 12px;
  border: 1px solid #e6ebf5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #e6ebf5;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.filter-container {
  margin-bottom: 20px;
}

.log-filter-form .el-form-item {
  margin-bottom: 0; /* 在 inline form 中减少下边距 */
}


/* 表格和分页 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 25px;
}

/* Vue 脚本部分保持不变 */
</style>

<script>
// 您原来的 <script> 内容可以完全不变地放在这里
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
    this.currentAdmin = localStorage.getItem("username") || '管理员';
    this.loginTime = new Date().toLocaleTimeString('zh-CN', { hour12: false });
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
        '回复评论': 'primary'
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
      }).replace(/\//g, '-');
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
        // 构建基础查询参数
        const params = new URLSearchParams();
        params.append('page', this.pagination.currentPage);
        params.append('size', this.pagination.pageSize);

        // 只有在用户主动设置了筛选条件时才添加到查询参数中
        if (this.logFilter.adminName?.trim()) {
          params.append('adminName', this.logFilter.adminName.trim());
        }
        if (this.logFilter.action) {
          params.append('actionType', this.logFilter.action);
        }
        if (this.logFilter.dateRange?.length === 2) {
          params.append('startTime', this.logFilter.dateRange[0]);
          params.append('endTime', this.logFilter.dateRange[1]);
        }

        const response = await axios.get(`http://localhost:8081/admin/logs?${params.toString()}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (response.data.code === '0') {
          this.adminLogs = response.data.data.records || [];
          this.pagination.total = response.data.data.total || 0;
        } else {
          console.error('获取日志失败:', response.data);
          ElMessage.error(response.data.msg || '获取日志失败');
        }
      } catch (error) {
        console.error('日志获取失败:', error);
        if (error.response?.data?.msg) {
          ElMessage.error('获取日志失败: ' + error.response.data.msg);
        } else {
          ElMessage.error('获取日志失败: ' + error.message);
        }
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