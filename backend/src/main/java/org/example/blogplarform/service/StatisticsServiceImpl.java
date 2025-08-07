package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.Dto.TagArticleCountDTO;
import org.example.blogplarform.Dto.UserGrowthDTO;
import org.example.blogplarform.mapper.ArticleMapper;
import org.example.blogplarform.mapper.CommentMapper;
import org.example.blogplarform.mapper.TagMapper;
import org.example.blogplarform.mapper.UserMapper;
import org.example.blogplarform.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userMapper.selectCount(null));
        map.put("adminCount", userMapper.selectCount(new QueryWrapper<User>().eq("role", "ADMIN")));
        map.put("articleCount", articleMapper.selectCount(null));
        map.put("tagCount", tagMapper.selectCount(null));
        map.put("commentCount", commentMapper.selectCount(null));
        map.put("viewCount", articleMapper.sumViewCount());
        map.put("likeCount", articleMapper.sumLikeCount());
        return map;
    }

    @Override
    public List<UserGrowthDTO> getUserGrowth() {
        List<UserGrowthDTO> raw = userMapper.getUserGrowthLast7Days();

        Map<String, Integer> dateToCount = raw.stream()
                .collect(Collectors.toMap(UserGrowthDTO::getDate, UserGrowthDTO::getCount));

        List<UserGrowthDTO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            int  count = dateToCount.getOrDefault(date, 0);
            result.add(new UserGrowthDTO(date, count));
        }

        return result;
    }

    @Override
    public List<TagArticleCountDTO> getTagArticleCount() {
        return tagMapper.getAllTagArticleCounts();
    }
}
