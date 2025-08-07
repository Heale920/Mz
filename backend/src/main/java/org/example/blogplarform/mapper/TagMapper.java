package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.blogplarform.Dto.TagArticleCountDTO;
import org.example.blogplarform.model.Tag;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    void addArticleTagRelation(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    @Select("SELECT \n" +
            "    t.name AS tagName,\n" +
            "    COUNT(at.article_id) AS articleCount\n" +
            "FROM \n" +
            "    tag t\n" +
            "LEFT JOIN \n" +
            "    article_tag at ON t.id = at.tag_id\n" +
            "GROUP BY \n" +
            "    t.id\n" +
            "ORDER BY \n" +
            "    articleCount DESC")
    List<TagArticleCountDTO> getAllTagArticleCounts();
}

