<template>
  <div class="notification-page">
    <div class="page-header">
      <div class="header-content">
        <h1>消息中心</h1>
        <p class="subtitle">查看您的最新通知、评论和互动</p>
      </div>
    </div>

    <div class="main-content">
      <div class="notification-filters">
        <div class="filter-group">
          <el-select v-model="notificationType" placeholder="全部消息类型" clearable @change="handleTypeChange">
            <el-option label="点赞" value="LIKE">
              <div class="option-content">
                <i class="el-icon-star-on" style="color: #FF9F43"></i>
                <span>点赞</span>
              </div>
            </el-option>
            <el-option label="评论" value="COMMENT">
              <div class="option-content">
                <i class="el-icon-chat-dot-round" style="color: #5B8FF9"></i>
                <span>评论</span>
              </div>
            </el-option>
            <el-option label="回复" value="REPLY">
              <div class="option-content">
                <i class="el-icon-chat-line-round" style="color: #47C269"></i>
                <span>回复</span>
              </div>
            </el-option>
          </el-select>
          <el-switch
            v-model="showOnlyUnread"
            active-text="只看未读"
            @change="handleUnreadChange"
            class="unread-switch"
          />
        </div>
        <el-button 
          type="primary" 
          @click="markAllAsRead" 
          v-if="hasUnread"
          class="mark-all-read-btn"
        >
          <i class="el-icon-check"></i>
          全部标记为已读
        </el-button>
      </div>

      <div class="notification-list" v-loading="loading">
        <template v-if="notifications.length > 0">
          <transition-group name="notification-fade" tag="div" class="notification-grid">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-card"
              :class="{
                'unread': !notification.isRead,
                'notification-new': notification.isNew
              }"
              @click="viewContent(notification)"
            >
              <div class="notification-header">
                <div class="notification-icon" :class="getNotificationTypeClass(notification.type)">
                  <i :class="getNotificationIcon(notification.type)"></i>
                </div>
                <el-tag 
                  size="small" 
                  :type="getNotificationTagType(notification.type)"
                  effect="light"
                  class="notification-tag"
                >
                  {{ getNotificationTypeText(notification.type) }}
                </el-tag>
                <div class="notification-time">{{ formatDate(notification.createdAt) }}</div>
              </div>
              
              <div class="notification-body">
                <h3 class="notification-title">{{ notification.title }}</h3>
                <p class="notification-text">{{ notification.content }}</p>
              </div>

              <div class="notification-footer">
                <div class="sender-info" v-if="notification.senderName">
                  <el-avatar 
                    :size="24" 
                    :src="notification.senderAvatar"
                    class="sender-avatar"
                  />
                  <span class="sender-name">{{ notification.senderName }}</span>
                </div>
                <div class="notification-status" v-if="!notification.isRead">
                  <el-badge is-dot class="status-dot" />
                  <span>未读</span>
                </div>
              </div>
            </div>
          </transition-group>
        </template>
        <el-empty 
          v-else 
          description="暂无消息" 
          :image-size="200"
        >
          <template #description>
            <p class="empty-text">还没有收到任何消息哦</p>
          </template>
        </el-empty>
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export default {
  name: 'NotificationPage',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const notifications = ref([])
    const notificationType = ref('')
    const showOnlyUnread = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const hasUnread = ref(false)
    const notificationSound = ref(null)
    let ws = null

    // 计算属性
    const currentUser = computed(() => localStorage.getItem('username') || '游客')
    const avatarWithTimestamp = computed(() => {
      const avatarPath = localStorage.getItem('avatar') || ''
      return avatarPath ? `${avatarPath}?t=${Date.now()}` : ''
    })

    // 获取通知列表
    const fetchNotifications = async () => {
      loading.value = true
      try {
          const response = await axios.get('http://localhost:8081/api/notifications/list', {
          params: {
            page: currentPage.value,
            size: pageSize.value,
            type: notificationType.value || undefined,
            isRead: showOnlyUnread.value ? false : undefined
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.code === '0') {
          notifications.value = response.data.data.records
          total.value = response.data.data.total
        }
      } catch (error) {
        console.error('获取通知失败:', error)
        ElMessage.error('获取通知失败')
      } finally {
        loading.value = false
      }
    }

    // 获取未读通知数量
    const fetchUnreadCount = async () => {
      try {
        const response = await axios.get('http://localhost:8081/api/notifications/unread/count', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.code === '0') {
          hasUnread.value = response.data.data > 0
        }
      } catch (error) {
        console.error('获取未读数量失败:', error)
      }
    }

    // 标记通知为已读
    const markAsRead = async (notificationId) => {
      try {
        await axios.put(`http://localhost:8081/api/notifications/${notificationId}/read`, null, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        await fetchNotifications()
        await fetchUnreadCount()
      } catch (error) {
        console.error('标记已读失败:', error)
        ElMessage.error('标记已读失败')
      }
    }

    // 标记所有通知为已读
    const markAllAsRead = async () => {
      try {
        await axios.put('http://localhost:8081/api/notifications/read/all', null, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        })
        ElMessage.success('已全部标记为已读')
        await fetchNotifications()
        await fetchUnreadCount()
      } catch (error) {
        console.error('标记全部已读失败:', error)
        ElMessage.error('标记全部已读失败')
      }
    }

    // 查看通知内容
    const viewContent = async (notification) => {
      if (!notification.isRead) {
        await markAsRead(notification.id)
      }

      // 根据通知类型和sourceType跳转到相应页面
      if (notification.sourceType === 'ARTICLE') {
        router.push(`/article/${notification.sourceId}`)
      }
    }

    // 获取通知图标
    const getNotificationIcon = (type) => {
      switch (type) {
        case 'LIKE':
          return 'el-icon-star-on'
        case 'COMMENT':
          return 'el-icon-chat-dot-round'
        case 'REPLY':
          return 'el-icon-chat-line-round'
        default:
          return 'el-icon-bell'
      }
    }

    // 获取通知类型样式
    const getNotificationTypeClass = (type) => {
      switch (type) {
        case 'LIKE':
          return 'notification-icon-like'
        case 'COMMENT':
          return 'notification-icon-comment'
        case 'REPLY':
          return 'notification-icon-reply'
        default:
          return ''
      }
    }

    // 获取通知标签类型
    const getNotificationTagType = (type) => {
      switch (type) {
        case 'LIKE':
          return 'warning'
        case 'COMMENT':
          return 'primary'
        case 'REPLY':
          return 'success'
        default:
          return 'info'
      }
    }

    // 获取通知类型文本
    const getNotificationTypeText = (type) => {
      switch (type) {
        case 'LIKE':
          return '点赞'
        case 'COMMENT':
          return '评论'
        case 'REPLY':
          return '回复'
        default:
          return '通知'
      }
    }

    // 格式化日期
    const formatDate = (date) => {
      const now = new Date()
      const notificationDate = new Date(date)
      const diff = now - notificationDate

      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) { // 24小时内
        return `${Math.floor(diff / 3600000)}小时前`
      } else if (diff < 604800000) { // 7天内
        return `${Math.floor(diff / 86400000)}天前`
      } else {
        return notificationDate.toLocaleDateString()
      }
    }

    // 处理类型变化
    const handleTypeChange = () => {
      currentPage.value = 1
      fetchNotifications()
    }

    // 处理未读筛选变化
    const handleUnreadChange = () => {
      currentPage.value = 1
      fetchNotifications()
    }

    // 处理每页条数变化
    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchNotifications()
    }

    // 处理页码变化
    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchNotifications()
    }

    // 导航方法
    const goToProfile = () => {
      const userId = localStorage.getItem('userId')
      if (userId) {
        router.push(`/user/${userId}`)
      }
    }

    const logout = () => {
      localStorage.clear()
      router.push('/login')
    }

    // 初始化WebSocket连接
    const initWebSocket = () => {
      const userId = localStorage.getItem('userId')
      if (!userId) return

      ws = new WebSocket(`ws://localhost:8081/websocket/notification/${userId}`)

      ws.onopen = () => {
        console.log('WebSocket连接已建立')
      }

      ws.onmessage = (event) => {
        const notification = JSON.parse(event.data)
        // 添加新通知标记
        notification.isNew = true
        // 播放提示音
        notificationSound.value?.play()
        // 显示通知提示
        ElMessage({
          message: notification.content,
          type: 'success',
          duration: 3000
        })
        // 更新通知列表
        if (!showOnlyUnread.value || !notification.isRead) {
          notifications.value.unshift(notification)
          // 3秒后移除新通知标记
          setTimeout(() => {
            notification.isNew = false
          }, 3000)
        }
        fetchUnreadCount()
      }

      ws.onclose = () => {
        console.log('WebSocket连接已关闭')
      }

      ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
      }
    }

    onMounted(() => {
      fetchNotifications()
      fetchUnreadCount()
      initWebSocket()
    })

    onUnmounted(() => {
      if (ws) {
        ws.close()
      }
    })

    return {
      loading,
      notifications,
      notificationType,
      showOnlyUnread,
      currentPage,
      pageSize,
      total,
      currentUser,
      avatarWithTimestamp,
      hasUnread,
      notificationSound,
      markAsRead,
      markAllAsRead,
      viewContent,
      getNotificationIcon,
      getNotificationTypeClass,
      getNotificationTagType,
      getNotificationTypeText,
      formatDate,
      handleTypeChange,
      handleUnreadChange,
      handleSizeChange,
      handleCurrentChange,
      goToProfile,
      logout
    }
  }
}
</script>

<style scoped>
.notification-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.page-header {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  padding: 40px 0;
  color: white;
  margin-bottom: 30px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content h1 {
  font-size: 32px;
  margin: 0;
  font-weight: 600;
}

.subtitle {
  margin: 10px 0 0;
  opacity: 0.8;
  font-size: 16px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.notification-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 20px;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-switch {
  margin-left: 16px;
}

.mark-all-read-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notification-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.notification-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.notification-card.unread {
  background: linear-gradient(to right, #f0f7ff, white);
  border-left: 4px solid #1890ff;
}

.notification-card.notification-new {
  animation: slideIn 0.5s ease-out;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.notification-icon-like {
  background-color: #fff2e8;
  color: #FF9F43;
}

.notification-icon-comment {
  background-color: #e6f7ff;
  color: #5B8FF9;
}

.notification-icon-reply {
  background-color: #f6ffed;
  color: #47C269;
}

.notification-time {
  margin-left: auto;
  font-size: 12px;
  color: #8c8c8c;
}

.notification-body {
  flex: 1;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px;
}

.notification-text {
  font-size: 14px;
  color: #595959;
  margin: 0;
  line-height: 1.6;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.sender-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sender-name {
  font-size: 13px;
  color: #8c8c8c;
}

.notification-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #ff4d4f;
}

.status-dot {
  margin-right: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin: 30px 0;
  padding: 20px 0;
}

.empty-text {
  color: #8c8c8c;
  font-size: 16px;
  margin-top: 12px;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 30px 0;
  }

  .header-content h1 {
    font-size: 24px;
  }

  .notification-filters {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }

  .notification-grid {
    grid-template-columns: 1fr;
  }

  .notification-card {
    margin-bottom: 16px;
  }
}
</style>