package org.example.blogplarform.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String coverImage;
    private String visibility = "PUBLIC"; // 文章可见性：PUBLIC（公开）, PRIVATE（私密）
    private Boolean isTop = false; // 是否置顶
    private Boolean isRecommend = false; // 是否推荐
    private String customLayout; // 自定义布局
    private String customStyle; // 自定义样式
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    private Integer status = 1;  // 文章状态：0-草稿，1-已发布
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer favoriteCount = 0; // 收藏量
    private Integer commentCount = 0; // 评论量
    private String tags; // 数据库中存储的标签字符串，格式：1,2,3

    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String authorName;
    
    @TableField(exist = false)
    private List<Long> tagList; // 用于前端交互的标签列表
    
    @TableField(exist = false)
    private User author;
    
    @TableField(exist = false)
    private Boolean hasLiked;
    
    @TableField(exist = false)
    private Boolean hasFavorited;


    // 在获取tagList时，将tags字符串转换为List<Long>
    public List<Long> getTagList() {
        if (StringUtils.isBlank(tags)) {
            return null;
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    // 在设置tagList时，将List<Long>转换为tags字符串
    public void setTagList(List<Long> tagList) {
        if (tagList != null && !tagList.isEmpty()) {
            this.tags = tagList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            this.tags = null;
        }
    }
}
