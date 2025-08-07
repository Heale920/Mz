package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Article;

import java.util.List;
import java.util.Map;

public interface ArticleService extends IService<Article> {

    Article getArticleById(Long id);
    List<Article> getArticlesByUserId(Long userId);
    List<Article> getArticlesByTagId(Long tagId);
    List<Article> getPublicArticles(String visibility); // 获取公开文章列表
    Article publishArticle(Article article);
    Result<?> incrementViewCount(Long articleId);//文章浏览量
    Result<?> toggleLike(Long articleId, Long userId);//点赞状态
    boolean hasLiked(Long articleId, Long userId);//检查用户是否已点赞
    void likeArticle(Long articleId, Long senderId);

    Result<?> updateArticle(Article article); // 更新文章
    Result<?> deleteArticle(Long articleId, Long userId); // 删除文章（需要验证作者身份）
    Result<List<Article>> searchArticles(String keyword, Integer page, Integer size);//查询

    //数据统计，用于个人中心中的数据展示
    Map<String, Object> getUserArticleStats(Long userId);

//    // 审核文章
//    void auditArticle(Long articleId, boolean approved, String reason);
//
//    // 获取待审核的文章列表
//    List<Article> getPendingArticles();
}
