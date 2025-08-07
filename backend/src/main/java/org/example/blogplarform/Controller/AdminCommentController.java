package org.example.blogplarform.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.blogplarform.annotation.AdminOperation;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.service.AdminLogService;
import org.example.blogplarform.service.CommentService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import org.example.blogplarform.model.CommentStatus;

@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AdminLogService adminLogService;

    /**
     * 获取评论列表（支持分页、筛选）
     */
    @GetMapping
    public Result<IPage<Comment>> getComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        IPage<Comment> comments = commentService.getAdminComments(page, size, status, keyword, startDate, endDate);
        return Result.success(comments);
    }

    @PutMapping("/{id}/approve")
    @AdminOperation(
        value = "通过评论",
        description = "管理员[%s]通过了评论ID[%s]",
        params = {"id"}
    )
    public Result<?> approveComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        commentService.updateCommentStatus(id, CommentStatus.APPROVED.getValue());
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @AdminOperation(
        value = "驳回评论",
        description = "管理员[%s]驳回了评论ID[%s]",
        params = {"id"}
    )
    public Result<?> rejectComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        commentService.updateCommentStatus(id, CommentStatus.REJECTED.getValue());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @AdminOperation(
        value = "删除评论",
        description = "管理员[%s]删除了评论ID[%s]",
        params = {"id"}
    )
    public Result<?> deleteComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        commentService.updateCommentStatus(id, CommentStatus.DELETED.getValue());
        return Result.success();
    }

//    @PostMapping("/{id}/reply")
//    @AdminOperation(
//        value = "回复评论",
//        description = "管理员[%s]回复了评论ID[%s]，回复内容：[%s]",
//        params = {"id", "content"}
//    )
//    public Result<?> replyToComment(
//            @PathVariable Long id,
//            @RequestBody Map<String, String> replyMap,
//            @RequestHeader("Authorization") String token) {
//        String content = replyMap.get("content");
//        commentService.adminReplyComment(id, content);
//        return Result.success();
//    }
} 