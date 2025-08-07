package org.example.blogplarform.Controller;



import org.example.blogplarform.annotation.AdminOperation;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.UserMapper;
import org.example.blogplarform.model.User;
import org.example.blogplarform.service.AdminLogService;
import org.example.blogplarform.service.AdminService;
import org.example.blogplarform.service.UserService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/user")
@CrossOrigin 
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AdminService adminService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private AdminLogService adminLogService;

    @PostMapping("/login")
    @AdminOperation(
        value = "管理员登录",
        description = "管理员[%s]登录系统",
        params = {}
    )
    public Result<Map<String, Object>> doLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        User user = userService.doLogin(username, password);
        if (user != null) {
            // 生成 Token
            String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 如果是管理员登录，记录日志已经由AOP处理

            // 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);  // 用户基本信息
            data.put("token", token); // Token

            return Result.success(data, "登录成功！");
        }
        return Result.error("123", "账号或密码错误！");
    }

    @GetMapping("/currentUser")
    public Result<User> getCurrentUser(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            System.out.println(user);
            return Result.success(user);
        } else {
            return Result.error("404", "用户不存在");
        }
    }
    @PutMapping("/{id}")
    public Result<User> updateUserInfo(
            @PathVariable Long id,
            @RequestPart("user") User updated,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) throws IOException {
        boolean ok = userService.updateUserInfo(id, updated, avatar);
        if (!ok) {
            return Result.error("404", "用户不存在或更新失败");
        }
        User updatedUser = userMapper.selectById(id);
        System.out.println(updatedUser);
        return Result.success(updatedUser, "用户信息更新成功");
    }

    @PostMapping("/doRegister")
    public Result<User> register(@RequestBody User newUser) {
        newUser.setRole("user");
        if (newUser.getEmail() == null || !newUser.getEmail().contains("@")) {
            return Result.error("400", "邮箱格式不正确！");
        }

        User user = userService.insert(newUser);
        if (user != null) {
            return Result.success(user, "注册成功！");
        } else {
            return Result.error("456", "用户名已存在！");
        }
    }

}


