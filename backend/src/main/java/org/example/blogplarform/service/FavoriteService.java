package org.example.blogplarform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Favorite;
import java.util.List;

public interface FavoriteService extends IService<Favorite> {
    // 添加收藏
    Result<?> addFavorite(Long articleId, Long userId);
    
    // 取消收藏
    Result<?> removeFavorite(Long articleId, Long userId);
    
    // 获取用户的收藏列表
    List<Favorite> getUserFavorites(Long userId);
    
    // 检查用户是否已收藏文章
    boolean hasFavorited(Long articleId, Long userId);
    
    // 获取文章的收藏数
    int getArticleFavoriteCount(Long articleId);
} 