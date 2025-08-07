package org.example.blogplarform.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long articleId;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    
    @TableField(exist = false)
    private Article article; // 关联的文章信息
} 