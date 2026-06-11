package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-orders")
public class HotelOrderController {

    @Autowired
    private HotelOrderService hotelOrderService;

    @GetMapping
    public List<HotelOrder> list() {
        return hotelOrderService.getAll();
    }

    @PostMapping
    public HotelOrder create(@RequestBody HotelOrder order) {
        return hotelOrderService.save(order);
    }

    // ======================
    // 🔥 新增：根据用户名查询订单（前端使用）
    // ======================
    @GetMapping("/user")
    public List<HotelOrder> getByUser(@RequestParam String username) {
        return hotelOrderService.getOrdersByUsername(username);
    }



    // 提交评价（稳定可用）
    @PostMapping("/comment/{id}")
    public HotelOrder comment(@PathVariable Long id, @RequestBody HotelOrder order) {
        HotelOrder old = hotelOrderService.getById(id);
        old.setComment(order.getComment());
        return hotelOrderService.save(old);
    }
}