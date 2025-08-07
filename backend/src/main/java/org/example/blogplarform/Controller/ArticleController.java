package org.example.blogplarform.Controller;


import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpServletRequest;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.CommentMapper;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Comment;
import org.example.blogplarform.service.ArticleService;
import org.example.blogplarform.service.UserService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private CommentMapper commentMapper;


    @GetMapping("/articleList")
    public Result<List<Article>> ArticleList() {
        // 获取所有公开的文章
        List<Article> articleList = articleService.getPublicArticles("PUBLIC");
        return Result.success(articleList);
    }

    @GetMapping("/{id}")
    public Result getArticleDetail(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.error("404", "文章不存在");
        }
        return Result.success(article);
    }

    // 获取用户所有文章
    @GetMapping("/user/{userId}")
    public Result<List<Article>> getArticlesByUser(@PathVariable Long userId, HttpServletRequest request) {
        System.out.println("正在查询用户ID: " + userId + " 的文章");
        
        // 获取当前登录用户ID
        String token = request.getHeader("Authorization");
        Long currentUserId = null;
        if (token != null && token.startsWith("Bearer ")) {
            currentUserId = JwtUtils.getUserIdFromToken(token.substring(7));
        }
        
        List<Article> articles;
        if (currentUserId != null && currentUserId.equals(userId)) {
            // 如果是查看自己的文章，显示所有文章（包括私密的）
            articles = articleService.getArticlesByUserId(userId);
        } else {
            // 如果是查看他人的文章，只显示公开的文章
            articles = articleService.getArticlesByUserId(userId);
        }
        
        System.out.println("查询到文章数量: " + articles.size());
        return Result.success(articles);
    }

    @GetMapping("/ArticleByTag")
    public Result GetArticleByTag(@RequestParam Long TagID) {
        List<Article> articles = articleService.getArticlesByTagId(TagID);
        return Result.success(articles);
    }

    @PostMapping("/publish")
    public Result publishArticle(@RequestBody Article article) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("401", "缺少或无效的 Authorization Header");
        }
        String token = authHeader.substring(7);
        Long userId = JwtUtils.getUserIdFromToken(token);
        System.out.println("Token 解析出的用户ID：" + userId);

        article.setAuthorId(userId);
        System.out.println("准备保存的文章作者ID：" + article.getAuthorId());

        Article published = articleService.publishArticle(article);
        return Result.success(published);
    }

    @PostMapping("/view/{id}")
    public Result<?> incrViewCount(@PathVariable Long id) {
        return articleService.incrementViewCount(id);
    }

    @PostMapping("/like/{id}")
    public Result<?> togLike(@PathVariable Long id, HttpServletRequest request) {
        //请求头获取Authorization
        String Header = request.getHeader("Authorization");
        if (Header == null || !Header.startsWith("Bearer")) {
            return Result.error("401", "Please login");
        }
        //解析token
        String token = Header.substring(7);
        Long userId = JwtUtils.getUserIdFromToken(token);

        return articleService.toggleLike(id, userId);
    }

    @GetMapping("/hasLiked/{id}")
    public Result<?> hasLiked(@PathVariable Long id, HttpServletRequest request) {
        String Header = request.getHeader("Authorization");
        if (Header == null || !Header.startsWith("Bearer")) {
            return Result.error("401", "Please login");
        }
        //解析token
        String token = Header.substring(7);
        Long userId = JwtUtils.getUserIdFromToken(token);

        return Result.success(articleService.hasLiked(id, userId));
    }

    // 更新文章
    @PutMapping("/{id}")
    public Result<?> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        // 从 token 中获取用户 ID
        String token = request.getHeader("Authorization");
        Long userId = JwtUtils.getUserIdFromToken(token);

        // 设置文章ID和作者ID
        article.setId(id);
        article.setAuthorId(userId);

        return articleService.updateArticle(article);
    }

    // 删除文章
    @DeleteMapping("/{id}")
    public Result<?> deleteArticle(@PathVariable Long id) {
        // 从 token 中获取用户 ID
        String token = request.getHeader("Authorization");
        Long userId = JwtUtils.getUserIdFromToken(token);

        return articleService.deleteArticle(id, userId);
    }

    // 搜索文章
    @GetMapping("/search")
    public Result<List<Article>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return articleService.searchArticles(keyword, page, size);
    }

    // 获取用户文章统计数据
    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getUserArticleStats(@PathVariable Long userId) {
        System.out.println(userId);
        Map<String, Object> stats = articleService.getUserArticleStats(userId);
        System.out.println(stats);
        return Result.success(stats);
    }

    // 设置文章可见性
    @PostMapping("/visibility/{id}")
    public Result<?> setVisibility(@PathVariable Long id, @RequestParam String visibility, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "请先登录");
        }
        Long userId = JwtUtils.getUserIdFromToken(token.substring(7));

        Article article = articleService.getById(id);
        if (article == null || !article.getAuthorId().equals(userId)) {
            return Result.error("403", "无权操作此文章");
        }

        article.setVisibility(visibility);
        articleService.updateById(article);
        return Result.success(null, "设置成功");
    }

    // 设置文章置顶状态
    @PutMapping("/top/{id}")
    public Result<?> setTopStatus(@PathVariable Long id, @RequestParam Boolean isTop) {
        String token = request.getHeader("Authorization");
        Long userId = JwtUtils.getUserIdFromToken(token);

        Article article = articleService.getById(id);
        if (article == null || !article.getAuthorId().equals(userId)) {
            return Result.error("403", "无权操作此文章");
        }
        article.setIsTop(isTop);

        articleService.updateById(article);
        return Result.success(null, isTop ? "文章已置顶" : "已取消置顶");
    }
}
