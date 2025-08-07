// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import App from '@/App'; // 示例主页组件
import NotFound from '@/NotFound';
import DashBoard from "@/Admin/DashBoard.vue";
import UserLogin from "@/Enter/UserLogin.vue";
import UserRegister from "@/Enter/UserRegister.vue";
import { useUserStore } from '@/stores/user'
import UserDashBoard  from "@/views/UserDashBoard.vue";
import ArticleDetail from "@/views/ArticleDetail.vue";
import PublishArticle from "@/views/PublishArticle.vue";
import UserInfo from "@/User/UserInfo.vue";
import NotifiCation from "@/views/NotifiCation.vue";
import ArticleEdit from "@/views/ArticleEdit.vue";
import UserManagement from "@/Admin/UserManagement.vue";
import ArticleManagement from "@/Admin/ArticleManagement.vue";
import TodayArticle from "@/Admin/TodayArticle.vue";
import CommentManagement from "@/Admin/CommentManagement.vue";
import TodayComment from "@/Admin/TodayComment.vue";
import PendingItems from "@/Admin/PendingItems.vue";
import TagManagement from "@/Admin/TagManagement.vue";
import DataStatistics from "@/Admin/DataStatistics.vue";


const routes = [
    { path: '/', component: App },
    { path: '/login', component: UserLogin },
    { path: '/register', component: UserRegister },
    { path: '/dashboard', component: DashBoard ,meta:{ requiresAuth: true, requiresAdmin: true}},
    { path: '/dashboard1', component: UserDashBoard ,meta:{requiresAuth:true}},
    {path:'/article/:id',component: ArticleDetail},
    {path:'/user/:id',component: UserInfo,meta: { requiresAuth: true }},
    {path:'/publish',component: PublishArticle},
    {path:'/notification',component: NotifiCation},
    {path:'/edit/:id',component: ArticleEdit},
    {path:'/management',component: UserManagement},
    {path:'/management1',component: ArticleManagement},
    {path:'/management2',component: CommentManagement, meta: {requiresAuth: true, requiresAdmin: true}},
    {path:'/management3',component: TagManagement, meta: {requiresAuth: true, requiresAdmin: true}},
    {path:'/pend',component: PendingItems, meta: {requiresAuth: true, requiresAdmin: true}},
    {path:'/todayComment',component: TodayComment, meta: {requiresAuth: true}},
    {path:'/todayArticle',component: TodayArticle},
    {path:'/data',component: DataStatistics,meta: {requiresAdmin: true}},
    { path: '/notfound', component: NotFound } // 404 路由,匹配所有未定义的路径
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});

export default router;

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  // 如果需要登录但没登录
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    return next('/login');
  }

  // 如果需要管理员权限，但不是管理员
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return next('/notfound');
  }

  // 正常放行
  next();
});


