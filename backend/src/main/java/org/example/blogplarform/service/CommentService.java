package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


public interface CommentService extends IService<Comment> {

    //获取文章评论树
    List<Comment> getCommentTree(Long articleId);
    //添加评论
    Comment addComment(Comment comment, Long userId);
    //删除评论(软删除)
    boolean deleteComment(Long commentId, Long userId);
    //获取评论回复
    List<Comment> getReplies(Long parentId);

//    //用户信息
    void setUserInfo(Comment comment);

    void commentArticle(Long articleId, Long senderId, String content);

    // 管理员评论管理相关方法
    IPage<Comment> getAdminComments(Integer page, Integer size, String status, String keyword, LocalDate startDate, LocalDate endDate);
    boolean updateCommentStatus(Long commentId, Integer status);
    Comment adminReplyComment(Long parentId, Long articleId, String content);
}
