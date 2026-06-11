package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.service.FlightOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-orders")
public class FlightOrderController {

    @Autowired
    private FlightOrderService flightOrderService;

    @GetMapping
    public List<FlightOrder> list() {
        return flightOrderService.getAll();
    }

    @PostMapping
    public FlightOrder create(@RequestBody FlightOrder order) {
        return flightOrderService.save(order);
    }

    @GetMapping("/user")
    public List<FlightOrder> getByUser(@RequestParam String username) {
        return flightOrderService.getOrdersByUsername(username);
    }
}