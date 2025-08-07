<template>
  <div class="user-section">
    <button class="notification-btn" @click="goToNotifications">
      <i class="icon-message"></i> 消息
    </button>
    <img 
      :src="avatarWithTimestamp" 
      class="user-avatar" 
      @click="goToProfile" 
      alt="用户头像"
      @error="handleAvatarError"
    />
    <span class="username">欢迎，{{ currentUser }}</span>
    <button @click="logout" class="logout-btn">退出</button>
  </div>
</template>

<script>
export default {
  computed: {
    currentUser() {
      return localStorage.getItem("username") || '游客'
    },
    avatarWithTimestamp() {
      const avatarPath = localStorage.getItem("avatar")
      if (!avatarPath) {
        return '/default-avatar.png' // 使用默认头像
      }
      // 如果avatarPath已经包含完整的URL，直接返回
      if (avatarPath.startsWith('http')) {
        return avatarPath
      }
      // 如果avatarPath以/开头，直接拼接
      if (avatarPath.startsWith('/')) {
        return `http://localhost:8081${avatarPath}?t=${this.avatarTimestamp}`
      }
      // 其他情况，添加/后拼接
      return `http://localhost:8081/${avatarPath}?t=${this.avatarTimestamp}`
    }
  },
  methods: {
    handleAvatarError(e) {
      // 头像加载失败时使用默认头像
      e.target.src = '/default-avatar.png'
    }
  }
}
</script> 