package org.example.blogplarform.Controller;

import org.example.blogplarform.constant.Result;
import org.example.blogplarform.model.Tag;
import org.example.blogplarform.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
@CrossOrigin
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("/TagList")
    public Result<List<Tag>> TagList(){
        List<Tag> tags = tagService.list();
        return Result.success(tags);
    }

    @PostMapping("/add")
    public Result<Tag> addTag(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("400", "标签名称不能为空");
        }
        return tagService.addTag(name.trim());
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteTag(@PathVariable Integer id) {
        return tagService.deleteTagById(id);
    }
}

