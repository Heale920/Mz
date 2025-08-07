package org.example.blogplarform.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;


    public LikeRecord() {}

    public LikeRecord(Long articleId, Long userId) {
        this.articleId = articleId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
