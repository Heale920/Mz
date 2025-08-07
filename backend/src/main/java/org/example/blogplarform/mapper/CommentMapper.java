package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.blogplarform.model.Comment;
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
