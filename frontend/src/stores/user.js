  // stores/user.js
  import { defineStore } from 'pinia'

  export const useUserStore = defineStore('user', {
    state: () => ({
      userId: localStorage.getItem('userId') || '', // 👈 加这个
      username: localStorage.getItem('username') || '',
      avatarUrl: localStorage.getItem('avatar') || '',
      isAdmin: localStorage.getItem('isAdmin') === 'true',
      isAuthenticated: !!localStorage.getItem('token'),
      token: localStorage.getItem('token') || ''
    }),

    actions: {
      loginSuccess(userInfo) {
        // 调试：打印接收到的数据
        console.log('loginSuccess 接收数据:', userInfo);

        this.userId = userInfo.id
        this.username = userInfo.username
        this.avatarUrl = userInfo.avatarUrl || ''
        this.isAdmin = userInfo.role === 'admin'
        this.isAuthenticated = true
        this.token = userInfo.token

        // 持久化存储
        localStorage.setItem('userId', this.userId)
        localStorage.setItem('username', this.username)
        localStorage.setItem('avatar', this.avatarUrl)
        localStorage.setItem('isAdmin', this.isAdmin.toString())
        localStorage.setItem('token', this.token)

         console.log('存储后的 localStorage:', localStorage); // 调试
      },

      setAvatar(avatarUrl) {
        this.avatarUrl = avatarUrl
        localStorage.setItem('avatar', avatarUrl)
      },

      logout() {
        this.userId = ''
        this.username = ''
        this.isAdmin = false
        this.isAuthenticated = false
        this.token = ''
        this.avatarUrl = ''

        // 清除本地
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('avatar')
        localStorage.removeItem('isAdmin')
        localStorage.removeItem('token')
      }
    }
  })
