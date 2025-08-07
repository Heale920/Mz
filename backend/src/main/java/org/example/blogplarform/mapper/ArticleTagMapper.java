package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.example.blogplarform.model.ArticleTag;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
    @Select("SELECT article_id FROM article_tag WHERE tag_id = #{tagId}")
    List<Long> selectArticleIdsByTagId(Long tagId);
    @Insert("INSERT INTO article_tag (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    void insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId}")
    void deleteByArticleId(Long articleId);
}