package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.mapper.ArticleMapper;
import org.example.blogplarform.mapper.CommentMapper;
import org.example.blogplarform.mapper.UserMapper;
import org.example.blogplarform.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 获取文章评论树
     */
    @Override
    public List<Comment> getCommentTree(Long articleId) {
        // 构建查询条件：获取该文章的所有顶级评论（parentId 为 null），状态为已通过
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, articleId)    // 匹配文章 ID
                .isNull(Comment::getParentId)            // 顶级评论没有父评论
                .eq(Comment::getStatus, CommentStatus.APPROVED.getValue())  // 状态为已通过的评论
                .orderByDesc(Comment::getCreateTime);     // 按创建时间倒序排列

        // 查询所有顶级评论
        List<Comment> comments = this.list(queryWrapper);

        // 添加子评论数量统计
        comments.forEach(comment -> {
            setUserInfo(comment);
            // 查询已通过的子评论数量
            Long replyCount = count(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getParentId, comment.getId())
                    .eq(Comment::getStatus, CommentStatus.APPROVED.getValue()));
            comment.setReplyCount(replyCount);
        });

        return comments;
    }

    /**
     * 添加评论
     */
    @Override
    public Comment addComment(Comment comment, Long userId) {
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING.getValue()); // 设置为待审核状态
        this.save(comment);
        
        setUserInfo(comment);
        return comment;
    }

    /**
     * 删除评论(软删除)
     */
    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        if (comment != null || comment.getUserId().equals(userId)) {
            comment.setStatus(CommentStatus.DELETED.getValue()); // 设置为已删除状态
            return this.updateById(comment);
        }
        return false;
    }

    /**
     * 获取评论回复
     */
    @Override
    public List<Comment> getReplies(Long parentId) {
        List<Comment> replies = baseMapper.selectList(
                new QueryWrapper<Comment>()
                        .eq("parent_id", parentId)
                        .eq("status", CommentStatus.APPROVED.getValue())  // 只获取已通过的回复
                        .orderByAsc("create_time")
        );
        // 遍历每条子评论，补全用户信息
        replies.forEach(this::setUserInfo);
        return replies;
    }

    /**
     * 设置用户信息
     */
    @Override
    public void setUserInfo(Comment comment) {
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            comment.setUsername(user.getUsername());
            comment.setAvatar(user.getAvatar());
        }
    }

    @Override
    public void commentArticle(Long articleId, Long senderId, String content) {
        Article article = articleMapper.selectById(articleId);
        Long receiverId = article.getAuthorId();

        // 保存评论
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(senderId);
        comment.setContent(content);
        comment.setStatus(CommentStatus.PENDING.getValue()); // 设置为待审核状态
        commentMapper.insert(comment);
    }

    @Override
    public IPage<Comment> getAdminComments(Integer page, Integer size, String status, String keyword, LocalDate startDate, LocalDate endDate) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Comment::getStatus, Integer.parseInt(status));
        }
        
        // 关键词搜索（评论内容或用户名）
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like(Comment::getContent, keyword)
                .or()
                .inSql(Comment::getUserId, "SELECT id FROM user WHERE username LIKE '%" + keyword + "%'")
            );
        }
        
        // 日期范围筛选
        if (startDate != null) {
            queryWrapper.ge(Comment::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.le(Comment::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Comment::getCreateTime);
        
        // 执行分页查询
        IPage<Comment> commentPage = this.page(pageParam, queryWrapper);
        
        // 补充用户信息和文章标题
        commentPage.getRecords().forEach(comment -> {
            setUserInfo(comment);
            
            // 设置文章标题
            Article article = articleMapper.selectById(comment.getArticleId());
            if (article != null) {
                comment.setArticleTitle(article.getTitle());
            }
            // 如果是回复评论，获取父评论内容
            if (comment.getParentId() != null) {
                Comment parentComment = this.getById(comment.getParentId());
                if (parentComment != null) {
                    comment.setParentContent(parentComment.getContent());
                }
            }
        });
        
        return commentPage;
    }

    @Override
    public boolean updateCommentStatus(Long commentId, Integer status) {
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setStatus(status);
            return this.updateById(comment);
        }
        return false;
    }

    @Override
    public Comment adminReplyComment(Long parentId, Long articleId, String content) {
        // 创建管理员回复
        Comment reply = new Comment();
        reply.setParentId(parentId);
        reply.setArticleId(articleId);
        reply.setContent(content);
        reply.setUserId(0L); // 使用特殊的用户ID表示管理员
        reply.setCreateTime(LocalDateTime.now());
        reply.setStatus(CommentStatus.APPROVED.getValue()); // 管理员回复默认为已通过状态
        
        this.save(reply);
        
        // 补充回复的用户信息
        setUserInfo(reply);
        
        return reply;
    }
}
