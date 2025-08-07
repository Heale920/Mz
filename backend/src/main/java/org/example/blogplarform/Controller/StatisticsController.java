package org.example.blogplarform.Controller;

import org.example.blogplarform.Dto.UserGrowthDTO;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result getOverView() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/usergrowth")
    public Result<List<UserGrowthDTO>> getuserGrowth() {
        return Result.success(statisticsService.getUserGrowth());
    }

    @GetMapping("/TagArticleCount")
    public Result getTagArticleCount() {
        return Result.success(statisticsService.getTagArticleCount());
    }
}
