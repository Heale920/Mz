package org.example.blogplarform.constant;

public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    REJECTED(3, "已拒绝");

    private final Integer value;
    private final String desc;

    ArticleStatus(Integer value, String desc) {
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