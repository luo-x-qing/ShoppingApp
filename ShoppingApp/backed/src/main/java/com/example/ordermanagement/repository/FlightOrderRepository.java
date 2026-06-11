package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.FlightOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FlightOrderRepository extends JpaRepository<FlightOrder, Long> {
    List<FlightOrder> findAllByOrderByIdDesc();
    List<FlightOrder> findByLoginUsernameOrderByIdDesc(String loginUsername);
}