package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface AdminService extends IService<User> {
    //分页获取用户，支持按角色筛选
    IPage<User> GetUserByPage(int page, int size, String role);
    //用户状态的改变
    boolean updateUserStatus(Long userId, Integer status);
    //用户角色的改变
    boolean updateUserRole(Long userId,String role);
    //根据id查找用户
    User GetUserById(Long userId);
    //搜索用户
    IPage<User> SearchUser(String keyword, int page, int size);
    //删除用户
    boolean deleteUser(Long userId);
    //获取所有的文章（包含私密文章）
    IPage<Article> GetAllArticle(int page,int size,Integer status);
    //管理员操员日志
    //获取今日发布文章的数据
    IPage<Article>GetTodayArticle(int page ,int size);

    //获取今日评论的数据
    IPage<Comment>GetTodayComment(int page,int size);
    //获取仪表盘统计数据
    Map<String, Object> getDashboardStats();
}
