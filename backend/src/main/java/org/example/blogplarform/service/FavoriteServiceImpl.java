package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.ArticleMapper;
import org.example.blogplarform.mapper.FavoriteMapper;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Favorite;
import org.example.blogplarform.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional
    public Result<?> addFavorite(Long articleId, Long userId) {
        // 检查是否已收藏
        if (hasFavorited(articleId, userId)) {
            return Result.error("400", "已经收藏过该文章");
        }

        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return Result.error("404", "文章不存在");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setArticleId(articleId);
        favorite.setUserId(userId);
        save(favorite);

        // 更新文章收藏数
        article.setFavoriteCount(article.getFavoriteCount() + 1);
        articleMapper.updateById(article);

        return Result.success(null, "收藏成功");
    }

    @Override
    @Transactional
    public Result<?> removeFavorite(Long articleId, Long userId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return Result.error("404", "文章不存在");
        }

        // 删除收藏记录
        boolean removed = remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getArticleId, articleId)
                .eq(Favorite::getUserId, userId));

        if (removed) {
            // 更新文章收藏数
            article.setFavoriteCount(article.getFavoriteCount() - 1);
            articleMapper.updateById(article);

            return Result.success(null, "取消收藏成功");
        }

        return Result.error("404", "收藏记录不存在");
    }

    @Override
    public boolean hasFavorited(Long articleId, Long userId) {
        return count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getArticleId, articleId)
                .eq(Favorite::getUserId, userId)) > 0;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId) {
        System.out.println("正在获取用户ID: " + userId + " 的收藏列表");
        
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);
        
        // 执行查询        
        List<Favorite> favorites = list(queryWrapper);
        System.out.println("从数据库查询到的原始收藏记录数: " + favorites.size());
                
        // 加载每个收藏对应的文章信息
        for (Favorite favorite : favorites) {
            System.out.println("正在加载文章ID: " + favorite.getArticleId() + " 的信息");
            Article article = articleMapper.selectById(favorite.getArticleId());
            if (article != null) {
                System.out.println("成功加载文章: " + article.getTitle());
            } else {
                System.out.println("警告：未找到文章ID: " + favorite.getArticleId());
            }
            favorite.setArticle(article);
        }
        
        System.out.println("获取到收藏数量: " + favorites.size());
        return favorites;
    }

    @Override
    public int getArticleFavoriteCount(Long articleId) {
        return (int)count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getArticleId, articleId));
    }
}