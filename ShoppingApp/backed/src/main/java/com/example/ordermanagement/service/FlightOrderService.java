package com.example.ordermanagement.service;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.repository.FlightOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FlightOrderService {

    @Autowired
    private FlightOrderRepository flightOrderRepository;

    public List<FlightOrder> getAll() {
        return flightOrderRepository.findAllByOrderByIdDesc();
    }

    public FlightOrder save(FlightOrder order) {
        return flightOrderRepository.save(order);
    }

    public List<FlightOrder> getOrdersByUsername(String username) {
        return flightOrderRepository.findByLoginUsernameOrderByIdDesc(username);
    }
}