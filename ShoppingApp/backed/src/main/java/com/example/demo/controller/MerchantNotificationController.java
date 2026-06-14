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
@RequestMapping("/api/merchant/notifications")
@CrossOrigin(origins = "*")
public class MerchantNotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<Notification>> getNotifications(@RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User merchant = userService.getUserByToken(tokenValue);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("无权访问");
            }
            
            List<Notification> notifications = notificationService.getMerchantNotifications(merchant.getId());
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@RequestHeader("Authorization") String token) {
        try {
            String tokenValue = token.substring(7);
            User merchant = userService.getUserByToken(tokenValue);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("无权访问");
            }
            
            long count = notificationService.getUnreadCount(merchant.getId());
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
            User merchant = userService.getUserByToken(tokenValue);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("无权访问");
            }
            
            notificationService.markAsRead(merchant.getId(), id);
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
            User merchant = userService.getUserByToken(tokenValue);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("无权访问");
            }
            
            notificationService.markAllAsRead(merchant.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
