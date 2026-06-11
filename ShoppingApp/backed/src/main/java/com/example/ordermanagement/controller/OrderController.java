package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        order.setType("酒店预订");
        return ResponseEntity.ok(orderService.createOrder(order));
    }
}