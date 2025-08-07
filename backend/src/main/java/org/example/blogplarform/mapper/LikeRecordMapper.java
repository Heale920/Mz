package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.blogplarform.model.LikeRecord;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    @Select(" SELECT COUNT(*) FROM like_record WHERE article_id = #{articleId} AND user_id = #{userId}")
    int countByArticleIdAndUserId(Long articleId, Long userId);//点赞
    @Delete(" DELETE FROM like_record WHERE article_id = #{articleId} AND user_id = #{userId}")
    int deleteByArticleIdAndUserId(Long articleId, Long userId);//取消点赞

}
