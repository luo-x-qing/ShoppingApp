package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Result;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.service.HotelService;
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

    @Autowired
    private HotelService hotelService;

    /**
     * 获取商家列表（分页+筛选）
     */
    @GetMapping
    public Result<Map<String, Object>> getMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        try {
            // 获取所有商家（role = MERCHANT）
            List<User> allMerchants = userService.getAllMerchants();

            // 筛选
            List<User> filtered = allMerchants;
            if (keyword != null && !keyword.isEmpty()) {
                filtered = filtered.stream()
                        .filter(m -> (m.getShopName() != null && m.getShopName().contains(keyword))
                                || (m.getUsername() != null && m.getUsername().contains(keyword))
                                || (m.getPhone() != null && m.getPhone().contains(keyword)))
                        .collect(Collectors.toList());
            }

            // 状态筛选
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                filtered = filtered.stream()
                        .filter(m -> status.equals(convertStatusForFilter(m.getStatus())))
                        .collect(Collectors.toList());
            }

            // 统计
            long total = filtered.size();
            long pendingCount = allMerchants.stream()
                    .filter(m -> "PENDING".equals(m.getStatus()) || m.getStatus() == null).count();
            long approvedCount = allMerchants.stream()
                    .filter(m -> "NORMAL".equals(m.getStatus())).count();
            long disabledCount = allMerchants.stream()
                    .filter(m -> "BANNED".equals(m.getStatus())).count();

            // 分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, (int) total);
            List<User> pagedList = filtered.subList(start, end);

            // 清除敏感信息（创建副本）
            List<User> safeList = pagedList.stream().map(m -> {
                User safe = new User();
                safe.setId(m.getId());
                safe.setUsername(m.getUsername());
                safe.setRole(m.getRole());
                safe.setStatus(m.getStatus());
                safe.setShopName(m.getShopName());
                safe.setPhone(m.getPhone());
                safe.setEmail(m.getEmail());
                safe.setCreateTime(m.getCreateTime());
                return safe;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("list", safeList);
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

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public Result<User> getMerchant(@PathVariable Long id) {
        try {
            User merchant = userService.getUserById(id);
            if (merchant != null && "MERCHANT".equals(merchant.getRole())) {
                // getUserById 已经返回了没有密码和token的副本
                return Result.success(merchant);
            } else {
                return Result.error("商家不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 审核商家（待审核 -> 正常 / 拒绝）
     */
    @PostMapping("/{id}/review")
    public Result<Map<String, Object>> reviewMerchant(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.get("approved");

            // 只验证商家是否存在，使用 getUserEntity 避免清空密码
            User merchant = userService.getUserEntity(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }

            boolean success;
            if (approved) {
                success = userService.approveMerchant(id);
            } else {
                String reason = request.containsKey("reason") ? (String) request.get("reason") : "不符合入驻条件";
                success = userService.rejectMerchant(id, reason);
            }

            if (!success) {
                return Result.error("审核操作失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 禁用商家（状态改为 BANNED）并发送通知，同时将该商家旗下所有酒店改为停业状态
     */
    @PutMapping("/{id}/disable")
    public Result<Map<String, Object>> disableMerchant(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : "违规经营";

            // 在禁用前获取商家基本信息（用于通知），使用 getUserEntity 获取原始实体
            User merchant = userService.getUserEntity(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }

            // 保存商家信息用于通知（在禁用操作之前）
            Long merchantId = merchant.getId();
            String merchantName = merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername();
            String merchantUsername = merchant.getUsername();

            // 1. 禁用商家
            boolean success = userService.disableMerchant(id);
            if (!success) {
                return Result.error("禁用失败");
            }

            // 2. 将该商家旗下的所有酒店状态改为"已停业"
            int updatedHotelCount = 0;
            try {
                List<Hotel> merchantHotels = hotelService.getHotelsByMerchant(id);
                for (Hotel hotel : merchantHotels) {
                    if (!"已停业".equals(hotel.getStatus())) {
                        hotel.setStatus("已停业");
                        hotelService.saveHotel(hotel);
                        updatedHotelCount++;
                    }
                }
                System.out.println("已将商家 " + merchantUsername + " 旗下的 " + updatedHotelCount + " 家酒店设为停业状态");
            } catch (Exception e) {
                System.err.println("更新酒店状态失败：" + e.getMessage());
                // 继续执行，不影响商家禁用操作
            }

            // 3. 发送通知给商家
            notificationService.sendMerchantBannedNotification(
                    merchantId,
                    merchantName,
                    reason
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("affectedHotels", updatedHotelCount);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("禁用失败：" + e.getMessage());
        }
    }

    /**
     * 启用商家（状态改为 NORMAL）并发送通知，同时将该商家旗下所有酒店改为营业中状态
     */
    @PutMapping("/{id}/enable")
    public Result<Map<String, Object>> enableMerchant(@PathVariable Long id) {
        try {
            // 在启用前获取商家基本信息（用于通知），使用 getUserEntity 获取原始实体
            User merchant = userService.getUserEntity(id);
            if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
                return Result.error("商家不存在");
            }

            // 保存商家信息用于通知（在启用操作之前）
            Long merchantId = merchant.getId();
            String merchantName = merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername();
            String merchantUsername = merchant.getUsername();

            // 1. 启用商家
            boolean success = userService.enableMerchant(id);
            if (!success) {
                return Result.error("启用失败");
            }

            // 2. 将该商家旗下的所有酒店状态改为"营业中"
            int updatedHotelCount = 0;
            try {
                List<Hotel> merchantHotels = hotelService.getHotelsByMerchant(id);
                for (Hotel hotel : merchantHotels) {
                    if ("已停业".equals(hotel.getStatus())) {
                        hotel.setStatus("营业中");
                        hotelService.saveHotel(hotel);
                        updatedHotelCount++;
                    }
                }
                System.out.println("已将商家 " + merchantUsername + " 旗下的 " + updatedHotelCount + " 家酒店恢复营业");
            } catch (Exception e) {
                System.err.println("更新酒店状态失败：" + e.getMessage());
                // 继续执行，不影响商家启用操作
            }

            // 3. 发送通知给商家
            notificationService.sendMerchantUnbannedNotification(
                    merchantId,
                    merchantName
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("affectedHotels", updatedHotelCount);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("启用失败：" + e.getMessage());
        }
    }

    /**
     * 删除商家
     */
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

    /**
     * 转换状态用于筛选
     */
    private String convertStatusForFilter(String dbStatus) {
        if ("NORMAL".equals(dbStatus)) return "approved";
        if ("BANNED".equals(dbStatus)) return "disabled";
        if ("PENDING".equals(dbStatus) || dbStatus == null) return "pending";
        return "all";
    }
}