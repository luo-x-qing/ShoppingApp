package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists, please choose another one.");
        }
        User registeredUser = userService.register(user.getUsername(), user.getPassword());
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
        if (loggedInUser != null) {
            String token = UUID.randomUUID().toString(); // 生成一个简单的token
            loggedInUser.setPassword(null); // 不返回密码
            return ResponseEntity.ok(loggedInUser);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<User> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(null);
        }
        String tokenValue = token.substring(7); // 去掉 "Bearer " 前缀
        User user = userService.getUserByToken(tokenValue);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    // 1. 查询所有用户（管理员接口）
    @GetMapping("/admin/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null)); // 不返回密码
        return ResponseEntity.ok(users);
    }

    // 2. 删除用户（管理员接口）
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 3. 更新用户信息（管理员接口）
    @PutMapping("/admin/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        if (updatedUser != null) {
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}