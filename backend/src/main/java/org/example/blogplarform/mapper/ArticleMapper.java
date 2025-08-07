package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.blogplarform.model.Article;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    //根据用户id来获取作品
    @Select("SELECT * FROM article WHERE author_id = #{userId}")
    List<Article> selectByUserId(Long userId);
    
    //查找公开的文章
    @Select("SELECT * FROM article WHERE visibility = #{visibility} ORDER BY create_time DESC")
    List<Article> selectPublicArticle(String visibility);


    //总浏览量和点赞量
    @Select("SELECT SUM(view_count) FROM article")
    Integer sumViewCount();

    @Select("SELECT SUM(like_count) FROM article")
    Integer sumLikeCount();
}
