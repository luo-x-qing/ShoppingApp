package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel-orders")
@CrossOrigin(origins = "*")
public class HotelOrderController {

    @Autowired
    private HotelOrderService hotelOrderService;

    @GetMapping
    public Result<List<HotelOrder>> list() {
        try {
            List<HotelOrder> orders = hotelOrderService.getAll();
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<HotelOrder> create(@RequestBody HotelOrder order) {
        try {
            if (order.getUsername() == null || order.getUsername().isEmpty()) {
                return Result.error("用户未登录");
            }
            if (order.getHotelId() == null) {
                return Result.error("酒店信息不完整");
            }
            if (order.getRoomTypeId() == null) {
                return Result.error("请选择房型");
            }
            if (order.getCheckIn() == null) {
                return Result.error("入住日期不能为空");
            }
            if (order.getCheckOut() == null) {
                return Result.error("退房日期不能为空");
            }

            HotelOrder savedOrder = hotelOrderService.createOrder(order);
            return Result.success(savedOrder);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/user")
    public Result<List<HotelOrder>> getByUser(@RequestParam String username) {
        try {
            List<HotelOrder> orders = hotelOrderService.getOrdersByUsername(username);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/pay")
    public Result<Map<String, Object>> payOrder(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.payOrder(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("支付失败，订单不存在或状态不正确");
            }
        } catch (Exception e) {
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/confirm")
    public Result<Map<String, Object>> confirmOrder(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.confirmOrder(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("确认失败，订单不存在或状态不正确");
            }
        } catch (Exception e) {
            return Result.error("确认失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/checkin")
    public Result<Map<String, Object>> confirmCheckIn(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.confirmCheckIn(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("确认入住失败");
            }
        } catch (Exception e) {
            return Result.error("确认入住失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.cancelOrder(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("取消失败，订单状态不正确");
            }
        } catch (Exception e) {
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    // ========== 用户申请取消 ==========
    @PostMapping("/{orderId}/cancel-request")
    public Result<Map<String, Object>> cancelRequest(@PathVariable Long orderId,
                                                      @RequestParam String username,
                                                      @RequestParam String reason) {
        try {
            boolean success = hotelOrderService.cancelOrderRequest(orderId, reason, username);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("申请取消失败");
            }
        } catch (Exception e) {
            return Result.error("申请取消失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel-approve")
    public Result<Map<String, Object>> approveCancel(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.approveCancel(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("同意取消失败");
            }
        } catch (Exception e) {
            return Result.error("同意取消失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/cancel-reject")
    public Result<Map<String, Object>> rejectCancel(@PathVariable Long orderId, @RequestParam String reason) {
        try {
            boolean success = hotelOrderService.rejectCancel(orderId, reason);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("拒绝取消失败");
            }
        } catch (Exception e) {
            return Result.error("拒绝取消失败：" + e.getMessage());
        }
    }

    @GetMapping("/merchant")
    public Result<List<HotelOrder>> getOrdersByMerchant(@RequestParam List<Long> hotelIds) {
        try {
            List<HotelOrder> orders = hotelOrderService.getOrdersByMerchant(hotelIds);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/merchant/orders")
    public Result<List<HotelOrder>> getOrdersByMerchantId(@RequestParam Long merchantId) {
        try {
            System.out.println("根据商家ID查询订单，merchantId: " + merchantId);
            List<HotelOrder> orders = hotelOrderService.getOrdersByMerchantId(merchantId);
            return Result.success(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/hotel-ids")
    public Result<List<HotelOrder>> getOrdersByHotelIds(@RequestParam List<Long> hotelIds) {
        try {
            List<HotelOrder> orders = hotelOrderService.getOrdersByMerchant(hotelIds);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/comment/{id}")
    public Result<HotelOrder> comment(@PathVariable Long id,
                                       @RequestParam String comment,
                                       @RequestParam Integer rating) {
        try {
            HotelOrder order = hotelOrderService.addComment(id, comment, rating);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            return Result.error("评价失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/checkout")
    public Result<Map<String, Object>> confirmCheckOut(@PathVariable Long orderId) {
        try {
            boolean success = hotelOrderService.confirmCheckOut(orderId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                return Result.success(result);
            } else {
                return Result.error("确认退房失败，订单状态不正确");
            }
        } catch (Exception e) {
            return Result.error("确认退房失败：" + e.getMessage());
        }
    }
}
