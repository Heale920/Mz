package org.example.blogplarform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Article;
import org.example.blogplarform.model.Tag;

public interface TagService extends IService<Tag> {
    //添加标签
    Result<Tag> addTag(String name);
    Result<?> deleteTagById(Integer id);
}
