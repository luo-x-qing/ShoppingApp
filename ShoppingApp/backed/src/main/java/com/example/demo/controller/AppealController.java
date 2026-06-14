package com.example.demo.controller;

import com.example.demo.model.Appeal;
import com.example.demo.model.AppealRequest;
import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.AppealService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appeals")
@CrossOrigin(origins = "*")
public class AppealController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAppeal(@RequestBody AppealRequest request) {
        try {
            Appeal appeal = appealService.submitAppeal(request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("appealId", appeal.getId());
            result.put("message", "申诉已提交，管理员会尽快处理");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAllAppeals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        try {
            List<Appeal> appeals = appealService.getAllAppeals();
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                appeals = appeals.stream()
                        .filter(a -> status.equals(a.getReplyStatus()))
                        .collect(Collectors.toList());
            }
            if (type != null && !type.isEmpty() && !"all".equals(type)) {
                appeals = appeals.stream()
                        .filter(a -> type.equals(a.getType()))
                        .collect(Collectors.toList());
            }
            if (keyword != null && !keyword.isEmpty()) {
                appeals = appeals.stream()
                        .filter(a -> (a.getUsername() != null && a.getUsername().contains(keyword))
                                || (a.getShopName() != null && a.getShopName().contains(keyword)))
                        .collect(Collectors.toList());
            }
            long total = appeals.size();
            long pendingCount = appealService.countPending();
            long processedCount = appealService.countProcessed();
            long rejectedCount = appealService.countRejected();
            Map<String, Object> result = new HashMap<>();
            result.put("list", appeals);
            result.put("total", total);
            result.put("pendingCount", pendingCount);
            result.put("processedCount", processedCount);
            result.put("rejectedCount", rejectedCount);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/{id}")
    public Result<Appeal> getAppealDetail(@PathVariable Long id) {
        try {
            Appeal appeal = appealService.getAppealById(id);
            if (appeal != null) {
                return Result.success(appeal);
            } else {
                return Result.error("申诉不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/pending")
    public Result<List<Appeal>> getPendingAppeals() {
        try {
            List<Appeal> appeals = appealService.getPendingAppeals();
            return Result.success(appeals);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/reply/{id}")
    public Result<Map<String, Object>> replyAppeal(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reply = request.get("reply");
            String replyStatus = request.get("replyStatus");
            if (reply == null || reply.trim().isEmpty()) {
                return Result.error("请填写回复内容");
            }
            Appeal appeal = appealService.replyAppeal(id, reply, replyStatus);
            if (appeal == null) {
                return Result.error("申诉不存在");
            }
            User merchant = userService.findByUsername(appeal.getUsername());
            if ("PROCESSED".equals(replyStatus)) {
                if (merchant != null && "MERCHANT".equals(merchant.getRole())) {
                    merchant.setStatus("NORMAL");
                    userService.updateUser(merchant.getId(), merchant);
                }
                if (merchant != null) {
                    notificationService.sendAppealReplyNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            "申诉已通过",
                            "您的申诉已通过审核，账号已恢复正常。",
                            reply
                    );
                }
            } else if ("REJECTED".equals(replyStatus)) {
                if (merchant != null) {
                    notificationService.sendAppealReplyNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            "申诉未通过",
                            "您的申诉未被通过。",
                            reply
                    );
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "回复成功");
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("回复失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/statistics")
    public Result<Map<String, Long>> getStatistics() {
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", (long) appealService.getAllAppeals().size());
            stats.put("pending", appealService.countPending());
            stats.put("processed", appealService.countProcessed());
            stats.put("rejected", appealService.countRejected());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{id}")
    public Result<Map<String, Object>> deleteAppeal(@PathVariable Long id) {
        try {
            appealService.deleteAppeal(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/{username}")
    public Result<List<Appeal>> getUserAppeals(@PathVariable String username) {
        try {
            List<Appeal> appeals = appealService.getUserAppeals(username);
            return Result.success(appeals);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}