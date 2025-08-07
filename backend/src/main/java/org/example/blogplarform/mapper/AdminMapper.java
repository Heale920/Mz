package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.blogplarform.model.User;

@Mapper
public interface AdminMapper extends BaseMapper<User> {
}
