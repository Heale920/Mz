package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.TagMapper;
import org.example.blogplarform.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService{
    @Autowired
    TagMapper tagMapper;
    @Override
    public Result<Tag> addTag(String name) {
        // 检查标签名是否为空
        if (name == null || name.trim().isEmpty()) {
            return Result.error("400", "标签名不能为空");
        }

        // 检查标签是否已存在
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name.trim());
        Tag existingTag = tagMapper.selectOne(queryWrapper);
        if (existingTag != null) {
            return Result.error("405", "标签已存在");
        }

        // 创建新标签
        Tag tag = new Tag();
        tag.setName(name.trim());
        int flag = tagMapper.insert(tag);
        if (flag > 0) {
            return Result.success(tag);
        } else {
            return Result.error("500", "添加标签失败");
        }
    }
        @Override
        public Result<?> deleteTagById(Integer id) {
            // 先检查该标签是否存在
            Tag tag = tagMapper.selectById(id);
            if (tag == null) {
                return Result.error("404", "标签不存在");
            }

            // 删除标签
            int result = tagMapper.deleteById(id);
            if (result > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error("500", "删除失败");
            }
        }
    }
