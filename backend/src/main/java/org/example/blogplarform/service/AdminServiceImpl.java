package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.mapper.AdminMapper;
import org.example.blogplarform.mapper.CommentMapper;
import org.example.blogplarform.mapper.UserMapper;
import org.example.blogplarform.mapper.ArticleMapper;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, User> implements AdminService{

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CommentMapper commentMapper;
   @Autowired
   private AdminLogService adminLogService;

    @Override
    //分页查询
    public IPage<User> GetUserByPage(int page, int size, String role) {
        Page<User> pages = new Page<>(page, size);
        //条件构造器
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // 如果指定了角色，就按角色筛选
        if (role != null && ! role.isEmpty()) {
            wrapper.eq("role", role);
        }
        return adminMapper.selectPage(pages, wrapper);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        if (status != 0 && status != 1) return false;

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            return this.updateById(user);
        }
        return false;
    }


    @Override
    public boolean updateUserRole(Long userId, String role) {
        if (role == null ) return false;

        User user = userMapper.selectById(userId);
        if (user != null){
            user.setRole(role);
            return this.updateById(user);
        }
        return false;
    }

    @Override
    public User GetUserById(Long userId) {
        return adminMapper.selectById(userId);
    }

    @Override
    public IPage<User> SearchUser(String keyword, int page, int size) {
        Page<User> pages = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("username", keyword)  // 精确匹配用户名
                .or()
                .like("username", "%" + keyword + "%")  // 模糊匹配用户名
                .or()
                .like("email", "%" + keyword + "%"));   // 模糊匹配邮箱
        return adminMapper.selectPage(pages, wrapper);
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            int flag = userMapper.deleteById(user);
            return flag == 1;
        }
        return false;
    }

    @Override
    public IPage<Article> GetAllArticle(int page, int size, Integer status) {
        Page<Article> pages = new Page<>(page,size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status",1);

        IPage<Article> articlePage = articleMapper.selectPage(pages,wrapper);

        // 为每篇文章填充作者信息
        articlePage.getRecords().forEach(article -> {
            if (article.getAuthorId() != null) {
                User author = userMapper.selectById(article.getAuthorId());
                if (author != null) {
                    article.setAuthorName(author.getUsername());
                }
            }
        });
        return articlePage;
    }


    @Override
    public IPage<Article> GetTodayArticle(int page, int size) {
        Page<Article> pages = new Page<>(page,size);
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        wrapper.eq("status",1)
                .ge("create_Time",today)
                .lt("create_Time",tomorrow);
        IPage<Article> articlePage = articleMapper.selectPage(pages,wrapper);
        
        // 为每篇文章填充作者信息
        articlePage.getRecords().forEach(article -> {
            if (article.getAuthorId() != null) {
                User author = userMapper.selectById(article.getAuthorId());
                if (author != null) {
                    article.setAuthorName(author.getUsername());
                }
            }
        });
        
        return articlePage;
    }


    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取管理员数量
        QueryWrapper<User> adminCount = new QueryWrapper<>();
        adminCount.eq("role","admin");
        stats.put("adminCount", adminMapper.selectCount(adminCount));
        
        // 获取今日发布的文章数量
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        QueryWrapper<Article> todayArticleWrapper = new QueryWrapper<>();
        todayArticleWrapper.ge("create_time", today);
        stats.put("todayArticleCount", articleMapper.selectCount(todayArticleWrapper));
        
        // 获取今日评论数量
        QueryWrapper<Comment> todayCommentWrapper = new QueryWrapper<>();
        todayCommentWrapper.ge("create_time", today);
        stats.put("todayCommentCount", commentMapper.selectCount(todayCommentWrapper));
        
        return stats;
    }

    @Override
    public IPage<Comment> GetTodayComment(int page, int size) {
        Page<Comment> pages = new Page<>(page,size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        wrapper.eq("status",1)
                .ge("create_Time",today)
                .lt("create_Time",tomorrow);
        IPage<Comment> commentPage = commentMapper.selectPage(pages,wrapper);

        commentPage.getRecords().forEach(comment -> {
            if (comment.getUserId()!=null){
                User CommentAuthor = userMapper.selectById(comment.getUserId());
                if (CommentAuthor != null){
                    comment.setUsername(CommentAuthor.getUsername());
                }
            }
        });

        return commentPage;
    }
}
