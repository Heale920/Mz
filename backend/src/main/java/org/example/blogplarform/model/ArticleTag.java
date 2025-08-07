package org.example.blogplarform.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 实体类
@Data
@TableName("article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
}