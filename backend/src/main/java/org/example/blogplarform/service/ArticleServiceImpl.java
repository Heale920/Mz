package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.*;
import org.example.blogplarform.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;


@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper,Article> implements ArticleService{

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private  LikeRecordMapper likeRecordMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null && article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                article.setAuthorName(author.getUsername());
            }
        }
        return article;
    }

    @Override
    public List<Article> getArticlesByUserId(Long userId) {
        // 创建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getAuthorId, userId)
               .and(w -> w.eq(Article::getVisibility, "PUBLIC")
                         .or()
                         .eq(Article::getAuthorId, userId)); // 作者可以看到自己的所有文章
        
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Article> getArticlesByTagId(Long tagId) {
        List<Long> articleIds = articleTagMapper.selectArticleIdsByTagId(tagId);
        if (articleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectBatchIds(articleIds);
    }
    @Override
    @Transactional
    public Article publishArticle(Article article) {
        // 保存文章
        articleMapper.insert(article);
        Long articleId = article.getId();
        
        // 处理标签关联
        List<Long> tagIds = article.getTagList();
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                try {
                    articleTagMapper.insertArticleTag(articleId, tagId);
                    System.out.println("成功关联文章ID:" + articleId + " 与标签ID:" + tagId);
                } catch (Exception e) {
                    log.error("关联文章标签失败", e);
                    throw new RuntimeException("关联标签失败", e);
                }
            }
        } else {
            log.warn("文章没有关联任何标签");
        }

        return article;
    }

    @Override
    public Result<?> incrementViewCount(Long articleId) {
        //查文章
        Article article = articleMapper.selectById(articleId);
        if (article == null )
            return Result.error("401","文章不存在");

        article.setViewCount(article.getViewCount()+1);

        articleMapper.updateById(article);

        return Result.success(article.getViewCount());
    }

    @Override
    public Result<?> updateArticle(Article article) {
        // 1. 验证文章是否存在
        Article existingArticle = articleMapper.selectById(article.getId());
        if (existingArticle == null) {
            return Result.error("404", "文章不存在");
        }
        
        // 2. 验证是否是作者本人
        if (!existingArticle.getAuthorId().equals(article.getAuthorId())) {
            return Result.error("403", "只有作者才能修改文章");
        }
        
        // 3. 更新文章基本信息
        existingArticle.setTitle(article.getTitle());
        existingArticle.setContent(article.getContent());
        existingArticle.setUpdateTime(new Date());
        existingArticle.setTagList(article.getTagList()); // 这里会自动转换为tags字符串
        
        // 4. 更新标签关联
        List<Long> tagIds = article.getTagList();
        if (tagIds != null && !tagIds.isEmpty()) {
            // 先删除原有的标签关联
            articleTagMapper.deleteByArticleId(article.getId());
            // 添加新的标签关联
            for (Long tagId : tagIds) {
                articleTagMapper.insertArticleTag(article.getId(), tagId);
            }
        }
        
        // 5. 保存更新
        articleMapper.updateById(existingArticle);
        return Result.success(existingArticle);
    }

    @Override
    public Result<?> deleteArticle(Long articleId, Long userId) {
        // 1. 验证文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return Result.error("404", "文章不存在");
        }
        
        // 2. 验证是否是作者本人
        if (!article.getAuthorId().equals(userId)) {
            return Result.error("403", "只有作者才能删除文章");
        }
        
        // 3. 删除文章相关数据
        // 3.1 删除文章标签关联
        articleTagMapper.deleteByArticleId(articleId);
        
        // 3.2 删除文章评论（如果有评论相关的通知，也应该删除）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getArticleId, articleId);
        commentMapper.delete(commentWrapper);
        
        // 3.3 删除文章
        articleMapper.deleteById(articleId);
        
        return Result.success(null, "文章删除成功");
    }

    @Override
    public Result<?> toggleLike(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null)
            return Result.error("401", "文章不存在");

        boolean is_liked = likeRecordMapper.countByArticleIdAndUserId(articleId, userId) > 0;

        if (!is_liked) {
            LikeRecord likeRecord = new LikeRecord();
            likeRecord.setArticleId(articleId);
            likeRecord.setUserId(userId);
            likeRecordMapper.insert(likeRecord);
            article.setLikeCount(article.getLikeCount()+1);

        } else {
            likeRecordMapper.deleteByArticleIdAndUserId(articleId,userId);
            article.setLikeCount(article.getLikeCount()-1);
        }
        articleMapper.updateById(article);

        return Result.success(article.getLikeCount());
    }

    @Override
    public boolean hasLiked(Long articleId, Long userId) {
       //检查点赞记录表中是否存在该用户对该文章的点赞记录

        return likeRecordMapper.countByArticleIdAndUserId(articleId,userId)>0;
    }

    @Override
    public void likeArticle(Long articleId, Long likerId) {
        Article article = articleMapper.selectById(articleId);
        Long receiverId = article.getAuthorId();

        // 检查是否已点赞
        LambdaQueryWrapper<LikeRecord> like = new LambdaQueryWrapper<>();
        like.eq(LikeRecord::getArticleId, articleId)
            .eq(LikeRecord::getUserId, likerId);

        LikeRecord record = likeRecordMapper.selectOne(like);
        if (record != null) {
            System.out.println("已点赞");
            return;  // 如果已经点赞，直接返回
        }

        // 添加点赞记录
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setArticleId(articleId);
        likeRecord.setUserId(likerId);
        likeRecordMapper.insert(likeRecord);

    }

    @Override
    public Result<List<Article>> searchArticles(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper
            .like(StringUtils.isNotBlank(keyword), Article::getTitle, keyword)
            .or()
            .like(StringUtils.isNotBlank(keyword), Article::getContent, keyword);

        Page<Article> articlePage = new Page<>(page, size);
        articleMapper.selectPage(articlePage, wrapper);

        return Result.success(articlePage.getRecords(), "搜索成功");
    }

    @Override
    public Map<String, Object> getUserArticleStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 获取用户的所有文章
        List<Article> userArticles = getArticlesByUserId(userId);
        
        // 2. 计算统计数据
        int totalArticles = userArticles.size();
        int totalViews = userArticles.stream().mapToInt(Article::getViewCount).sum();
        int totalLikes = userArticles.stream().mapToInt(Article::getLikeCount).sum();
        
        // 3. 获取用户收藏的文章数量
        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getUserId, userId);
        long totalFavorites = favoriteMapper.selectCount(favoriteWrapper);
        
        // 4. 获取评论总数
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(Comment::getArticleId, 
            userArticles.stream().map(Article::getId).collect(Collectors.toList()));
        long totalComments = commentMapper.selectCount(commentWrapper);
        
        // 5. 封装统计数据
        stats.put("totalArticles", totalArticles);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        stats.put("totalComments", totalComments);
        stats.put("totalFavorites", totalFavorites);
        
        return stats;
    }

    // 获取公开文章列表
    @Override
    public List<Article> getPublicArticles(String visibility) {
       return articleMapper.selectPublicArticle(visibility);
    }
}
