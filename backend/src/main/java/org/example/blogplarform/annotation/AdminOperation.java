package org.example.blogplarform.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminOperation {
    String value() default "";  // 操作类型
    String description() default ""; // 操作描述模板，例如："管理员[%s]将用户[%s]的状态修改为[%s]"
    String[] params() default {}; // 用于描述的参数名列表
} 