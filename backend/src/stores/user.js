import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    avatar: localStorage.getItem('avatar') || '',
    isAdmin: localStorage.getItem('isAdmin') === 'true',
    isAuthenticated: !!localStorage.getItem('token'),
    token: localStorage.getItem('token') || ''
  }),

  actions: {
    loginSuccess(userInfo) {
      console.log('loginSuccess 接收数据:', userInfo)

      this.userId = userInfo.id
      this.username = userInfo.username
      this.avatar = userInfo.avatar || ''
      this.isAdmin = userInfo.role === 'admin'
      this.isAuthenticated = true
      this.token = userInfo.token

      // 持久化存储
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('username', this.username)
      localStorage.setItem('avatar', this.avatar)
      localStorage.setItem('isAdmin', this.isAdmin.toString())
      localStorage.setItem('token', this.token)

      console.log('存储后的 localStorage:', localStorage)
    },

    setAvatar(avatar) {
      this.avatar = avatar
      localStorage.setItem('avatar', avatar)
    },

    logout() {
      this.userId = ''
      this.username = ''
      this.isAdmin = false
      this.isAuthenticated = false
      this.token = ''
      this.avatar = ''

      // 清除本地存储
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('avatar')
      localStorage.removeItem('isAdmin')
      localStorage.removeItem('token')
    }
  }
}) 