package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/user")
    public List<HotelOrder> getByUser(@RequestParam String username) {
        return hotelOrderService.getOrdersByUsername(username);
    }

    @PostMapping("/comment/{id}")
    public HotelOrder comment(@PathVariable Long id, @RequestBody HotelOrder order) {
        HotelOrder old = hotelOrderService.getById(id);
        old.setComment(order.getComment());
        return hotelOrderService.save(old);
    }

    @GetMapping("/merchant/{merchantId}")
    public List<HotelOrder> getMerchantOrders(@PathVariable Long merchantId) {
        return hotelOrderService.getOrdersByMerchant(merchantId);
    }

    @GetMapping("/hotel-ids")
    public List<HotelOrder> getOrdersByHotelIds(@RequestParam List<Long> hotelIds) {
        return hotelOrderService.getOrdersByHotelIds(hotelIds);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelOrderService.confirmOrder(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@PathVariable Long id) {
        HotelOrder order = hotelOrderService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (order != null) {
            order.setStatus("已入住");
            hotelOrderService.save(order);
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long id) {
        HotelOrder order = hotelOrderService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (order != null) {
            order.setStatus("已完成");
            hotelOrderService.save(order);
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<Map<String, Object>> approveCancel(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelOrderService.approveCancel(id));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<Map<String, Object>> rejectCancel(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelOrderService.rejectCancel(id));
        return ResponseEntity.ok(result);
    }
}