package org.example.blogplarform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService  extends IService<User> {
    public User doLogin(String username,String password);
    public User findByUsername(String username);
    public User  insert(User user);
    boolean updateUserInfo(Long id, User updated , MultipartFile file) throws IOException;


}
