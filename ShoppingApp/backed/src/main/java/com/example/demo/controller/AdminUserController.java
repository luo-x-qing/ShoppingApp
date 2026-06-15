package com.example.demo.controller;

import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            List<User> allUsers = userService.getAllNormalUsers();
            List<User> filtered = allUsers;
            if (keyword != null && !keyword.isEmpty()) {
                filtered = filtered.stream()
                        .filter(u -> u.getUsername() != null && u.getUsername().contains(keyword))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                filtered = filtered.stream()
                        .filter(u -> status.equals(u.getStatus()))
                        .collect(Collectors.toList());
            }
            long total = filtered.size();
            long normalCount = allUsers.stream().filter(u -> "NORMAL".equals(u.getStatus())).count();
            long bannedCount = allUsers.stream().filter(u -> "BANNED".equals(u.getStatus())).count();
            int start = (page - 1) * size;
            int end = Math.min(start + size, (int) total);
            List<User> pagedList = filtered.subList(start, end);
            pagedList.forEach(u -> {
                u.setPassword(null);
                u.setToken(null);
            });
            Map<String, Object> result = new HashMap<>();
            result.put("list", pagedList);
            result.put("total", total);
            Map<String, Long> counts = new HashMap<>();
            counts.put("total", (long) allUsers.size());
            counts.put("normal", normalCount);
            counts.put("banned", bannedCount);
            result.put("counts", counts);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                user.setPassword(null);
                user.setToken(null);
                return Result.success(user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/ban")
    public Result<Map<String, Object>> banUser(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String, Object> request) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            String reason = request != null && request.containsKey("reason") ? (String) request.get("reason") : "违规发言";
            int durationDays = 0;
            if (request != null && request.containsKey("durationDays")) {
                durationDays = (Integer) request.get("durationDays");
            }
            boolean success = userService.banUser(id, reason, durationDays);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return success ? Result.success(result) : Result.error("禁言失败");
        } catch (Exception e) {
            return Result.error("禁言失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/unban")
    public Result<Map<String, Object>> unbanUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            boolean success = userService.unbanUser(id);
            if (success) {
                notificationService.sendUserUnbannedNotification(
                        user.getId(),
                        user.getUsername()
                );
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return success ? Result.success(result) : Result.error("解除禁言失败");
        } catch (Exception e) {
            return Result.error("解除禁言失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}