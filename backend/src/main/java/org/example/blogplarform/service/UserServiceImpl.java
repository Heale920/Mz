package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.constant.Result;
import org.example.blogplarform.mapper.UserMapper;
import org.example.blogplarform.model.User;
//import org.example.blogplarform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminLogService adminLogService;

    @Override
    public User findByUsername(String username) {
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("username",username);
      return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateUserInfo(Long id, User updated, MultipartFile avatar) throws IOException {
        User newUser = userMapper.selectById(id);
        if (newUser == null) {
            return false;
        }
        // 如果传了新头像，就保存并替换
        if (avatar != null && !avatar.isEmpty()) {
            String avatarPath = this.saveAvatar(avatar);
            newUser.setAvatar(avatarPath);
        }

        // 更新其他基本信息
        if (updated.getUsername() != null)  newUser.setUsername(updated.getUsername());
        if (updated.getEmail() != null) newUser.setEmail(updated.getEmail());

        int rows = userMapper.updateById(newUser);
        return rows > 0;
    }

    public String saveAvatar(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String fileExt = originalName.substring(originalName.lastIndexOf("."));
        if (!fileExt.equals(".jpg") && !fileExt.equals(".jpeg") && !fileExt.equals(".png")) {
            throw new IllegalArgumentException("仅支持JPEG/JPG/PNG格式");
        }
//        if (file.getSize() > 2 * 1024 * 1024) {
//            throw new IllegalArgumentException("文件不能超过2MB");
//        }

        String fileName = UUID.randomUUID() + fileExt;

        // 保存到 resources/static/upload/avatar/
        String uploadDir = "./uploads/avatar/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 注意返回路径不要带/static
        return "/upload/avatar/" + fileName;
    }

    @Override
    public User doLogin(String username,String password) {
        User userInDb = this.getOne(new QueryWrapper<User>().eq("username", username));
        if (userInDb!=null&&password.equals(userInDb.getPassword())){
            return userInDb;
        }
        return null;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW) // 新事务确保立即提交
    @Override
    public User insert(User user) {
        User userInDb = this.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (userInDb != null) {
            return null;
        } else {
            userMapper.insert(user); // 插入后MyBatis会自动填充ID（需配置好主键策略）
            return this.getById(user.getId());
        }
    }

//    @Override
//    public Result<String> login(User user, HttpServletRequest request) {
//        // 1.根据用户名查询用户
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("username", user.getUsername());
//        User loginUser = userMapper.selectOne(queryWrapper);
//
//        // 2.判断是否存在
//        if (loginUser == null) {
//            return Result.error("404", "用户名不存在");
//        }
//
//        // 3.密码比对
//        if (!loginUser.getPassword().equals(user.getPassword())) {
//            return Result.error("403", "密码错误");
//        }
//
//        // 4.查看用户状态
//        if (loginUser.getStatus() == 0) {
//            return Result.error("403", "该用户已被禁用");
//        }
//
//        // 5.登录成功，生成token
//        String token = JwtUtil.createToken(loginUser);
//
//        // 6.如果是管理员登录，记录日志
//        if ("admin".equals(loginUser.getRole())) {
//            String ipAddress = request.getRemoteAddr();
//            adminLogService.recordLog(
//                loginUser.getId(),
//                loginUser.getUsername(),
//                "LOGIN",
//                "管理员登录系统",
//                ipAddress
//            );
//        }
//
//        return Result.success(token);
//    }
}
