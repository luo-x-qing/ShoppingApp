package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
}