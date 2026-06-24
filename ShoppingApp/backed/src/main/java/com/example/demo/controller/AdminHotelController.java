package com.example.demo.controller;

import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/hotels")
@CrossOrigin(origins = "*")
public class AdminHotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取酒店统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatistics() {
        try {
            List<Hotel> allHotels = hotelService.getAllHotels();

            long pending = allHotels.stream()
                    .filter(h -> "PENDING".equals(h.getStatus()) || h.getStatus() == null)
                    .count();
            long approved = allHotels.stream()
                    .filter(h -> "APPROVED".equals(h.getStatus()) || "营业中".equals(h.getStatus()))
                    .count();
            long violation = allHotels.stream()
                    .filter(h -> "VIOLATION".equals(h.getStatus()) || "违规".equals(h.getStatus()))
                    .count();
            long suspended = allHotels.stream()
                    .filter(h -> "SUSPENDED".equals(h.getStatus()) || "已停业".equals(h.getStatus()))
                    .count();

            Map<String, Long> stats = new HashMap<>();
            stats.put("total", (long) allHotels.size());
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("violation", violation);
            stats.put("suspended", suspended);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取待办事项列表
     */
    @GetMapping("/todo")
    public Result<List<Map<String, String>>> getTodoList() {
        try {
            List<Map<String, String>> todoList = new ArrayList<>();
            List<Hotel> allHotels = hotelService.getAllHotels();

            // 待审核酒店
            long pendingCount = allHotels.stream()
                    .filter(h -> "PENDING".equals(h.getStatus()) || h.getStatus() == null)
                    .count();
            if (pendingCount > 0) {
                Map<String, String> todo = new HashMap<>();
                todo.put("content", "有 " + pendingCount + " 家酒店待审核");
                todo.put("url", "/admin/hotels?tab=pending");
                todoList.add(todo);
            }

            // 违规酒店
            long violationCount = allHotels.stream()
                    .filter(h -> "VIOLATION".equals(h.getStatus()) || "违规".equals(h.getStatus()))
                    .count();
            if (violationCount > 0) {
                Map<String, String> todo = new HashMap<>();
                todo.put("content", "有 " + violationCount + " 家酒店违规，请处理");
                todo.put("url", "/admin/hotels?tab=violation");
                todoList.add(todo);
            }

            return Result.success(todoList);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取最近违规记录
     */
    @GetMapping("/recent-violations")
    public Result<List<Map<String, String>>> getRecentViolations() {
        try {
            List<Map<String, String>> violations = new ArrayList<>();
            List<Hotel> allHotels = hotelService.getAllHotels();

            List<Hotel> violationHotels = allHotels.stream()
                    .filter(h -> "VIOLATION".equals(h.getStatus()) || "违规".equals(h.getStatus()))
                    .collect(Collectors.toList());

            for (Hotel hotel : violationHotels) {
                Map<String, String> v = new HashMap<>();
                v.put("id", String.valueOf(hotel.getId()));
                v.put("name", hotel.getName());
                v.put("reason", hotel.getViolationReason() != null ? hotel.getViolationReason() : "违规经营");
                v.put("time", hotel.getUpdateTime() != null ? hotel.getUpdateTime().toString() : "未知");
                violations.add(v);
            }

            if (violations.size() > 5) {
                violations = violations.subList(0, 5);
            }

            return Result.success(violations);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    // ========== 酒店禁用/启用接口（带通知功能） ==========

    /**
     * 获取所有酒店列表（管理员用）
     */
    @GetMapping("/list")
    public Result<List<Hotel>> getAllHotels() {
        try {
            List<Hotel> hotels = hotelService.getAllHotels();
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取酒店详情
     */
    @GetMapping("/{id}")
    public Result<Hotel> getHotel(@PathVariable Long id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel != null) {
                return Result.success(hotel);
            } else {
                return Result.error("酒店不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 禁用酒店（状态改为 已停业）并发送通知
     */
    @PutMapping("/{id}/disable")
    public Result<Map<String, Object>> disableHotel(@PathVariable Long id,
                                                    @RequestBody(required = false) Map<String, String> request) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel == null) {
                return Result.error("酒店不存在");
            }

            String reason = request != null ? request.get("reason") : "违规经营";

            // 修改酒店状态
            hotel.setStatus("已停业");
            hotelService.saveHotel(hotel);

            // 获取商家信息并发送通知
            if (hotel.getMerchantId() != null) {
                User merchant = userService.getUserById(hotel.getMerchantId());
                if (merchant != null) {
                    notificationService.sendHotelBannedNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            hotel.getId(),
                            hotel.getName(),
                            reason
                    );
                    System.out.println("已发送酒店禁用通知给商家: " + merchant.getUsername());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "酒店已禁用，已通知商家");
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("禁用失败：" + e.getMessage());
        }
    }

    /**
     * 启用酒店（状态改为 营业中）并发送通知
     */
    @PutMapping("/{id}/enable")
    public Result<Map<String, Object>> enableHotel(@PathVariable Long id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel == null) {
                return Result.error("酒店不存在");
            }

            // 修改酒店状态
            hotel.setStatus("营业中");
            hotelService.saveHotel(hotel);

            // 获取商家信息并发送通知
            if (hotel.getMerchantId() != null) {
                User merchant = userService.getUserById(hotel.getMerchantId());
                if (merchant != null) {
                    notificationService.sendHotelUnbannedNotification(
                            merchant.getId(),
                            merchant.getShopName() != null ? merchant.getShopName() : merchant.getUsername(),
                            hotel.getId(),
                            hotel.getName()
                    );
                    System.out.println("已发送酒店启用通知给商家: " + merchant.getUsername());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "酒店已启用，已通知商家");
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("启用失败：" + e.getMessage());
        }
    }

    /**
     * 删除酒店
     */
    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}