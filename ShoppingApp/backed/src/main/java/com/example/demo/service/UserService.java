package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入加密器

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 注册时加密密码
    public User register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        // 用BCrypt加密
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    // 登录时校验加密后的密码
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return null;

        // 用matches方法校验，而不是equals
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        // 登录成功，生成token
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);
        return user;
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    // 查询所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 删除用户
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 更新用户信息
    public User updateUser(Long id, User newUser) {
        User oldUser = userRepository.findById(id).orElse(null);
        if (oldUser == null) return null;

        // 更新用户名
        if (newUser.getUsername() != null) {
            oldUser.setUsername(newUser.getUsername());
        }
        // 如果前端传了新密码，需要加密后更新
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        return userRepository.save(oldUser);
    }
}
/*package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    // 注入数据库仓库
    @Autowired
    private UserRepository userRepository;

    // 按用户名查找（给注册校验用）
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 注册：保存到数据库
    public User register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return userRepository.save(user); // 保存到数据库
    }

    // 登录：从数据库查询用户
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);

        // 安全判断，绝对不会空指针
        if (user == null) {
            return null;
        }
        if (!password.equals(user.getPassword())) {
            return null;
        }

        // 登录成功，生成token
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user); // 更新token到数据库

        user.setPassword(null); // 不返回密码
        return user;
    }

    // 根据token获取用户
    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }
}*/