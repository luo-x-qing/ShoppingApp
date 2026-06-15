package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
@CrossOrigin(origins = "*")
public class UserNotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<Notification>> getNotifications(@RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User user = userService.getUserByToken(tokenValue);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(user.getId());
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User user = userService.getUserByToken(tokenValue);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            long count = notificationService.getUserUnreadCount(user.getId());
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public Result<Map<String, Object>> markAsRead(@PathVariable Long id, 
                                                   @RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User user = userService.getUserByToken(tokenValue);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            notificationService.markUserNotificationAsRead(user.getId(), id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public Result<Map<String, Object>> markAllAsRead(@RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User user = userService.getUserByToken(tokenValue);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            notificationService.markAllUserNotificationsAsRead(user.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
