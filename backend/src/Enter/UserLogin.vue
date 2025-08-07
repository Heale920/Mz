<template>
  <!-- No changes to template section -->
</template>

<script>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const handleLogin = async (form) => {
      try {
        const res = await userStore.login(form)

        userStore.loginSuccess({
          id: res.data.user.id,
          username: res.data.user.username,
          avatar: res.data.user.avatar,
          token: res.data.token,
          role: res.data.user.role,
        })

        ElMessage.success('登录成功！')
        const role = res.data.user.role
        router.push(role === 'admin' ? '/dashboard' : '/dashboard1')

        reset()
      } catch (error) {
        console.error('登录失败:', error)
      }
    }

    const reset = () => {
      // Implementation of reset function
    }

    return {
      handleLogin,
      reset
    }
  }
}
</script>

<style>
  /* No changes to style section */
</style> 