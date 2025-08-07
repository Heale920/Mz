import { createRouter, createWebHistory } from 'vue-router';
import CommentManagement from '@/Admin/CommentManagement.vue'
import PendingItems from '@/Admin/PendingItems.vue'
import TagManagement from '@/Admin/TagManagement.vue'

const routes = [
    {
        path: '/tag',
        name: 'TagManagement',
        component: TagManagement,
        meta: {
            requiresAuth: true,
            requiresAdmin: true
        }
    },
    {
        path: '/comment',
        name: 'CommentManagement',
        component: CommentManagement,
        meta: {
            requiresAuth: true,
            requiresAdmin: true
        }
    },
    {
        path: '/pending-items',
        name: 'PendingItems',
        component: PendingItems,
        meta: {
            requiresAuth: true,
            requiresAdmin: true
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

// 全局导航守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

    if (requiresAuth && !token) {
        // 如果需要认证但没有token，重定向到登录页
        next('/login');
    } else if (requiresAdmin) {
        // 如果需要管理员权限，检查用户角色
        const userRole = localStorage.getItem('userRole');
        if (userRole !== 'admin') {
            next('/login');
        } else {
            next();
        }
    } else {
        next();
    }
});

export default router; 