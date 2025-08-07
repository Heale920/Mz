package org.example.blogplarform.model;

public enum CommentStatus {
    DELETED(0, "已删除"),
    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝");

    private final Integer value;
    private final String desc;

    CommentStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
} 