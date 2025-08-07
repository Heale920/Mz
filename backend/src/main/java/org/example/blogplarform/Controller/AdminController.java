package org.example.blogplarform.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Delete;
import org.example.blogplarform.annotation.AdminOperation;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.AdminLog;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.model.User;
import org.example.blogplarform.service.AdminLogService;
import org.example.blogplarform.service.AdminService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminLogService adminLogService;

    @GetMapping("/logs")
    public Result<IPage<AdminLog>> getAdminLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        try {
            IPage<AdminLog> logs = adminLogService.getAdminLogs(page, size, adminName, actionType, startTime, endTime);
            return Result.success(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取日志失败：" + e.getMessage());
        }
    }

    @GetMapping("/allUser")
    public Result<IPage<User>> getUserByPage(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String role) {

        IPage<User> userPage = adminService.GetUserByPage(page, size, role);
        return Result.success(userPage);
    }

    @GetMapping("/allArticle")
    public Result<IPage<Article>> getArticleByPage(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) Integer status){
        IPage<Article> articleIPage = adminService.GetAllArticle(page,size,status);
        return Result.success(articleIPage);
    }

    @GetMapping("/todayArticle")
    public Result<IPage<Article>> getTodayArticle(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size){
        IPage<Article> articleIPage = adminService.GetTodayArticle(page,size);
        return Result.success(articleIPage);
    }

    @GetMapping("/todayComment")
    public Result<IPage<Comment>> getTodayComment(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size){
        IPage<Comment> commentIPage = adminService.GetTodayComment(page,size);
        return Result.success(commentIPage);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    @PutMapping("/status/{id}")
    @AdminOperation(
        value = "更新用户状态",
        description = "管理员[%s]将用户ID[%s]的状态修改为[%s]",
        params = {"userId", "status"}
    )
    public Result<Void> updateUserStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long userId,
            @RequestBody Map<String, Integer> statusMap,
            HttpServletRequest request) {
        Integer status = statusMap.get("status");
        adminService.updateUserStatus(userId, status);
        return Result.success();
    }

    @PostMapping("/role/{id}")
    @AdminOperation(
        value = "更新用户角色",
        description = "管理员[%s]将用户ID[%s]的角色修改为[%s]",
        params = {"userId", "role"}
    )
    public Result<Void> updateUserRole(@PathVariable("id") Long userId,
                                     @RequestBody Map<String, String> roleMap,
                                     @RequestHeader("Authorization") String token) {
        String role = roleMap.get("role");
        adminService.updateUserRole(userId, role);
        return Result.success();
    }

    @DeleteMapping("/delUser/{id}")
    @AdminOperation(
        value = "删除用户",
        description = "管理员[%s]删除了用户ID[%s]",
        params = {"userId"}
    )
    public Result<Void> deleteUser(
            @PathVariable("id") Long userId,
            @RequestHeader("Authorization") String token) {
        adminService.deleteUser(userId);
        return Result.success();
    }

    @GetMapping("/search")
    public Result<IPage<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            IPage<User> userPage = adminService.SearchUser(keyword, page, size);
            return Result.success(userPage);
        } catch (Exception e) {
            return Result.error("500", "搜索用户失败：" + e.getMessage());
        }
    }
}
