package org.example.blogplarform.Controller;

import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    //获取文章评论树
    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getComments(@PathVariable Long articleId){
        List<Comment> commentTree = commentService.getCommentTree(articleId);
        return Result.success(commentTree);
    }

    //添加评论
    @PostMapping("/add")
    public Result<Comment> addComment(@RequestBody Comment comment, @RequestParam Long userId) {
        Comment savedComment = commentService.addComment(comment, userId);
        return Result.success(savedComment, "评论成功！");
    }

    //删除评论
    @DeleteMapping("/{commentId}")
    public Result<Comment> delComment(@PathVariable Long commentId, @RequestParam Long userId){
        boolean flag = commentService.deleteComment(commentId,userId);
        if (flag){
            return Result.success(null,"删除成功！");
        }else {
            return Result.error("401","无法删除或评论已不存在");
        }
    }

    //获取子评论(异步加载)
    @GetMapping("/replies/{parentId}")
    public Result<List<Comment>> getReplies(@PathVariable Long parentId){
        List<Comment> replies = commentService.getReplies(parentId);
        return Result.success(replies);
    }
}
