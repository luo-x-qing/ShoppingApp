package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.FlightOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-orders")
@CrossOrigin(origins = "*")
public class FlightOrderController {

    private static final Logger log = LoggerFactory.getLogger(FlightOrderController.class);

    @Autowired
    private FlightOrderService flightOrderService;

    @PostMapping
    public Result<FlightOrder> createOrder(@RequestBody FlightOrder order) {
        log.info("收到创建订单请求：{}", order);
        try {
            if (order.getUsername() == null || order.getUsername().isEmpty()) {
                return Result.error("用户未登录");
            }
            if (order.getPassengerName() == null || order.getPassengerName().isEmpty()) {
                return Result.error("乘客姓名不能为空");
            }
            if (order.getContactPhone() == null || order.getContactPhone().isEmpty()) {
                return Result.error("联系电话不能为空");
            }
            if (order.getFlightNumber() == null || order.getFlightNumber().isEmpty()) {
                return Result.error("航班信息不完整");
            }
            order.setStatus("待支付");
            FlightOrder savedOrder = flightOrderService.createOrder(order);
            log.info("订单创建成功，ID：{}", savedOrder.getId());
            return Result.success(savedOrder);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error("创建订单失败：" + e.getMessage());
        }
    }

    @GetMapping
    public Result<List<FlightOrder>> getOrdersByUsername(@RequestParam(required = false) String username) {
        log.info("查询订单，用户名：{}", username);
        try {
            List<FlightOrder> orders;
            if (username != null && !username.isEmpty()) {
                orders = flightOrderService.getOrdersByUsername(username);
            } else {
                orders = flightOrderService.getAllOrders();
            }
            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return Result.error("查询订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<FlightOrder> getOrderById(@PathVariable Long id) {
        log.info("查询订单详情，ID：{}", id);
        try {
            return flightOrderService.getOrderById(id)
                .map(Result::success)
                .orElse(Result.error("订单不存在"));
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return Result.error("查询订单失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/pay")
    public Result<FlightOrder> payOrder(@PathVariable Long id) {
        log.info("支付订单，ID：{}", id);
        try {
            FlightOrder order = flightOrderService.updateOrderStatus(id, "已支付");
            if (order != null) {
                log.info("订单支付成功，ID：{}", id);
                return Result.success(order);
            } else {
                return Result.error("订单不存在");
            }
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<FlightOrder> cancelOrder(@PathVariable Long id) {
        log.info("取消订单，ID：{}", id);
        try {
            FlightOrder order = flightOrderService.cancelOrder(id);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("订单不存在或无法取消");
            }
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<FlightOrder> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新订单状态，ID：{}，状态：{}", id, status);
        try {
            FlightOrder order = flightOrderService.updateOrderStatus(id, status);
            if (order != null) {
                return Result.success(order);
            } else {
                return Result.error("订单不存在");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        log.info("删除订单，ID：{}", id);
        try {
            boolean deleted = flightOrderService.deleteOrder(id);
            if (deleted) {
                return Result.success("删除成功");
            } else {
                return Result.error("订单不存在");
            }
        } catch (Exception e) {
            log.error("删除订单失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
