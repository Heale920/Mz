package org.example.blogplarform.Dto;

import lombok.Data;

@Data
public class UserGrowthDTO {
    private String date;//图表中的X轴
    private Integer count;//图表中的Y轴

    public UserGrowthDTO() {
    }

    public UserGrowthDTO(String date, Integer count) {
        this.date = date;
        this.count = count;
    }
}
