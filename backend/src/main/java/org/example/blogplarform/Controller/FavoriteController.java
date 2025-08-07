package org.example.blogplarform.Controller;

import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Favorite;
import org.example.blogplarform.service.FavoriteService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @PostMapping("/{articleId}")
    public Result<?> addFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "请先登录");
        }
        Long userId = JwtUtils.getUserIdFromToken(token.substring(7));
        return favoriteService.addFavorite(articleId, userId);
    }
    
    @DeleteMapping("/{articleId}")
    public Result<?> removeFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "请先登录");
        }
        Long userId = JwtUtils.getUserIdFromToken(token.substring(7));
        return favoriteService.removeFavorite(articleId, userId);
    }
    
    @GetMapping("/check/{articleId}")
    public Result<Boolean> checkFavoriteStatus(@PathVariable Long articleId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.success(false);
        }
        Long userId = JwtUtils.getUserIdFromToken(token.substring(7));
        return Result.success(favoriteService.hasFavorited(articleId, userId));
    }
    
    @GetMapping("/list/{userId}")
    public Result<List<Favorite>> getUserFavorites(@PathVariable Long userId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("401", "请先登录");
        }
        
        // 验证token中的用户ID与请求的用户ID是否匹配
        Long tokenUserId = JwtUtils.getUserIdFromToken(token.substring(7));
        
        if (!userId.equals(tokenUserId)) {
            return Result.error("403", "无权访问其他用户的收藏列表");
        }
        
        // 获取用户的收藏列表
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        System.out.println("成功获取收藏列表，数量: " + favorites.size());
        return Result.success(favorites);
    }
} 