package org.example.blogplarform.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
    private Long parentId;
    private Integer status = 1; // 0-已删除，1-待审核，2-已通过，3-已拒绝

    // 非数据库字段
    @TableField(exist = false)
    private String username; // 用户名
    @TableField(exist = false)
    private String avatar; // 用户头像
    @TableField(exist = false)
    private List<Comment> replies; // 回复列表
    @TableField(exist = false)
    private Long replyCount; // 子评论数量
    @TableField(exist = false)
    private String articleTitle; // 文章标题
    @TableField(exist = false)
    private String parentContent; // 父评论内容（用于显示回复的是哪条评论）
    @TableField(exist = false)
    private Boolean isAdminReply; // 是否是管理员回复
}

