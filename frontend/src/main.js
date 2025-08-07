import { createApp } from 'vue';
import BlogPlatform from "@/Blog-platform.vue";
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'  // <== 需要引入！

const app = createApp(BlogPlatform);

app.use(router);
app.use(ElementPlus);

const pinia = createPinia();
app.use(pinia);

// 💡 加上这段：让 userStore 重新读取本地数据
const userStore = useUserStore();
if (localStorage.getItem('token')) {
  userStore.isAuthenticated = true;
  userStore.username = localStorage.getItem('username') || '';
  userStore.token = localStorage.getItem('token') || '';
  userStore.isAdmin = localStorage.getItem('isAdmin') === 'true';
}

app.mount('#app');
