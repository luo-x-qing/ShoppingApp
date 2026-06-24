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

    /**
     * 用户提交申诉
     */
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

    // ========== 管理员接口 ==========

    /**
     * 管理员：获取所有申诉（支持筛选）
     */
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

    /**
     * 管理员：获取申诉详情
     */
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

    /**
     * 管理员：获取待处理申诉
     */
    @GetMapping("/admin/pending")
    public Result<List<Appeal>> getPendingAppeals() {
        try {
            List<Appeal> appeals = appealService.getPendingAppeals();
            return Result.success(appeals);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 管理员：回复申诉（唯一方法，已删除重复）
     */
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

            // 获取商家信息
            User merchant = userService.findByUsername(appeal.getUsername());

            // 根据处理结果处理
            if ("PROCESSED".equals(replyStatus)) {
                // 申诉通过：恢复商家账号状态
                if (merchant != null && "MERCHANT".equals(merchant.getRole())) {
                    merchant.setStatus("NORMAL");
                    userService.updateUser(merchant.getId(), merchant);
                    System.out.println("✅ 商家账号已恢复: " + appeal.getUsername());
                }

                // 发送通知给商家（申诉回复）
                if (merchant != null) {
                    notificationService.sendAppealReplyNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            "申诉已通过",
                            "您的申诉已通过审核，账号已恢复正常。",
                            reply
                    );
                    System.out.println("✅ 已发送申诉回复通知给商家: " + appeal.getUsername());
                }

            } else if ("REJECTED".equals(replyStatus)) {
                // 发送通知给商家（申诉回复）
                if (merchant != null) {
                    notificationService.sendAppealReplyNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            "申诉未通过",
                            "您的申诉未被通过。",
                            reply
                    );
                    System.out.println("✅ 已发送申诉回复通知给商家: " + appeal.getUsername());
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

    /**
     * 管理员：获取统计数据
     */
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

    /**
     * 管理员：删除申诉
     */
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

    // ========== 用户接口 ==========

    /**
     * 用户：获取自己的申诉记录
     */
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