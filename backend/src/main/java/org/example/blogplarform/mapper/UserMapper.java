package org.example.blogplarform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.blogplarform.Dto.UserGrowthDTO;
import org.example.blogplarform.model.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 最近7天的注册人数
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM user " +
            "WHERE create_time >= CURDATE() - INTERVAL 6 DAY " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<UserGrowthDTO> getUserGrowthLast7Days();

}