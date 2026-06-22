package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.model.SystemNotice;
import com.example.ordermanagement.service.SystemNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@CrossOrigin(origins = "*")
public class SystemNoticeController {

    @Autowired
    private SystemNoticeService systemNoticeService;

    /**
     * 获取用户所有通知
     */
    @GetMapping("/user/{username}")
    public Result<List<SystemNotice>> getUserNotices(@PathVariable String username) {
        try {
            List<SystemNotice> notices = systemNoticeService.getUserNotices(username);
            return Result.success(notices);
        } catch (Exception e) {
            return Result.error("获取通知失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/user/{username}/unread-count")
    public Result<Map<String, Object>> getUnreadCount(@PathVariable String username) {
        try {
            long count = systemNoticeService.getUnreadCount(username);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            systemNoticeService.markAsRead(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/user/{username}/read-all")
    public Result<Map<String, Object>> markAllAsRead(@PathVariable String username) {
        try {
            systemNoticeService.markAllAsRead(username);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}