package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Result;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/merchants")
@CrossOrigin(origins = "*")
public class AdminMerchantController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> getMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            List<User> allMerchants = userService.getAllMerchants();
            List<User> filtered = allMerchants;
            if (keyword != null && !keyword.isEmpty()) {
                filtered = filtered.stream()
                        .filter(m -> (m.getShopName() != null && m.getShopName().contains(keyword))
                                || (m.getUsername() != null && m.getUsername().contains(keyword))
                                || (m.getPhone() != null && m.getPhone().contains(keyword)))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                filtered = filtered.stream()
                        .filter(m -> status.equals(convertStatusForFilter(m.getStatus())))
                        .collect(Collectors.toList());
            }
            long total = filtered.size();
            long pendingCount = allMerchants.stream()
                    .filter(m -> "PENDING".equals(m.getStatus()) || m.getStatus() == null).count();
            long approvedCount = allMerchants.stream()
                    .filter(m -> "NORMAL".equals(m.getStatus())).count();
            long disabledCount = allMerchants.stream()
                    .filter(m -> "BANNED".equals(m.getStatus())).count();
            int start = (page - 1) * size;
            int end = Math.min(start + size, (int) total);
            List<User> pagedList = filtered.subList(start, end);
            pagedList.forEach(m -> {
                m.setPassword(null);
                m.setToken(null);
            });
            Map<String, Object> result = new HashMap<>();
            result.put("list", pagedList);
            result.put("total", total);
            Map<String, Long> counts = new HashMap<>();
            counts.put("total", (long) allMerchants.size());
            counts.put("pending", pendingCount);
            counts.put("approved", approvedCount);
            counts.put("disabled", disabledCount);
            result.put("counts", counts);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> getMerchant(@PathVariable Long id) {
        try {
            User merchant = userService.getUserById(id);
            if (merchant != null && "MERCHANT".equals(merchant.getRole())) {
                merchant.setPassword(null);
                merchant.setToken(null);
                return Result.success(merchant);
            } else {
                return Result.error("商家不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/review")
    public Result<Map<String, Object>> reviewMerchant(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");
            User merchant = userService.getUserById(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }
            if (approved) {
                merchant.setStatus("NORMAL");
            } else {
                merchant.setStatus("REJECTED");
            }
            userService.updateUser(id, merchant);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/disable")
    public Result<Map<String, Object>> disableMerchant(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, String> request) {
        try {
            User merchant = userService.getUserById(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }
            String reason = request != null ? request.get("reason") : "违规经营";
            merchant.setStatus("BANNED");
            userService.updateUser(id, merchant);
            notificationService.sendMerchantBannedNotification(
                    merchant.getId(),
                    merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                    reason
            );
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("禁用失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/enable")
    public Result<Map<String, Object>> enableMerchant(@PathVariable Long id) {
        try {
            User merchant = userService.getUserById(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }
            merchant.setStatus("NORMAL");
            userService.updateUser(id, merchant);
            notificationService.sendMerchantUnbannedNotification(
                    merchant.getId(),
                    merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername()
            );
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("启用失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteMerchant(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    private String convertStatusForFilter(String dbStatus) {
        if ("NORMAL".equals(dbStatus)) return "approved";
        if ("BANNED".equals(dbStatus)) return "disabled";
        if ("PENDING".equals(dbStatus) || dbStatus == null) return "pending";
        return "all";
    }
}