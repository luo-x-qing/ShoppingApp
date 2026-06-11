package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByIdDesc();
}