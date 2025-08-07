package org.example.blogplarform.service;

import org.example.blogplarform.Dto.TagArticleCountDTO;
import org.example.blogplarform.Dto.UserGrowthDTO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    Map<String,Object> getOverview();
    List<UserGrowthDTO> getUserGrowth();
    List<TagArticleCountDTO> getTagArticleCount();
}
