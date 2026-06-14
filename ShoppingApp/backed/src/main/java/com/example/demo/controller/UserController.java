package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        if (userService.findByUsername(user.getUsername()) != null) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(response);
        }
        User registeredUser = userService.registerUser(user.getUsername(), user.getPassword());
        response.put("success", true);
        response.put("message", "注册成功");
        response.put("userId", registeredUser.getId());
        response.put("role", "USER");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-merchant")
    public ResponseEntity<Map<String, Object>> registerMerchant(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        if (userService.findByUsername(user.getUsername()) != null) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(response);
        }
        if (user.getShopName() == null || user.getShopName().isEmpty()) {
            response.put("success", false);
            response.put("message", "请填写商家名称");
            return ResponseEntity.badRequest().body(response);
        }
        User merchant = userService.registerMerchant(
                user.getUsername(),
                user.getPassword(),
                user.getShopName(),
                user.getPhone()
        );
        response.put("success", true);
        response.put("message", "商家注册成功，请等待管理员审核");
        response.put("merchantId", merchant.getId());
        response.put("role", "MERCHANT");
        response.put("status", merchant.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
        if (loggedInUser != null) {
            System.out.println("登录用户: " + loggedInUser.getUsername());
            System.out.println("用户状态: " + loggedInUser.getStatus());
            System.out.println("用户角色: " + loggedInUser.getRole());
            loggedInUser.setPassword(null);
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
        String tokenValue = token.substring(7);
        User user = userService.getUserByToken(tokenValue);
        if (user != null) {
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestHeader("Authorization") String token,
                                              @RequestBody User userInfo) {
        String tokenValue = token.substring(7);
        User currentUser = userService.getUserByToken(tokenValue);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(null);
        }
        User updated = userService.updateUser(currentUser.getId(), userInfo);
        if (updated != null) {
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("/check-banned")
    public ResponseEntity<Map<String, Object>> checkBanned(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        String tokenValue = token.substring(7);
        User user = userService.getUserByToken(tokenValue);
        if (user == null) {
            response.put("isBanned", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }
        response.put("isBanned", "BANNED".equals(user.getStatus()));
        response.put("userId", user.getId());
        response.put("role", user.getRole());
        response.put("status", user.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merchant/info")
    public ResponseEntity<User> getMerchantInfo(@RequestHeader("Authorization") String token) {
        String tokenValue = token.substring(7);
        User user = userService.getUserByToken(tokenValue);
        if (user == null || !"MERCHANT".equals(user.getRole())) {
            return ResponseEntity.status(403).body(null);
        }
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/merchant/info")
    public ResponseEntity<User> updateMerchantInfo(@RequestHeader("Authorization") String token,
                                                   @RequestBody User userInfo) {
        String tokenValue = token.substring(7);
        User merchant = userService.getUserByToken(tokenValue);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return ResponseEntity.status(403).body(null);
        }
        User updated = userService.updateMerchantInfo(merchant.getId(), userInfo);
        if (updated != null) {
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("/merchant/check")
    public ResponseEntity<Map<String, Object>> checkMerchant(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        String tokenValue = token.substring(7);
        User user = userService.getUserByToken(tokenValue);
        if (user == null) {
            response.put("isMerchant", false);
            response.put("message", "未登录");
            return ResponseEntity.ok(response);
        }
        response.put("isMerchant", "MERCHANT".equals(user.getRole()));
        response.put("merchantId", user.getId());
        response.put("shopName", user.getShopName());
        response.put("status", user.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<User>> getAllMerchants() {
        List<User> merchants = userService.getAllMerchants();
        merchants.forEach(m -> {
            m.setPassword(null);
            m.setToken(null);
        });
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/merchant/{id}")
    public ResponseEntity<User> getMerchantById(@PathVariable Long id) {
        User merchant = userService.getUserById(id);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return ResponseEntity.notFound().build();
        }
        merchant.setPassword(null);
        merchant.setToken(null);
        return ResponseEntity.ok(merchant);
    }
}