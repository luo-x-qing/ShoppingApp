package com.example.ordermanagement.service;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.repository.HotelOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class HotelOrderService {

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    public List<HotelOrder> getAll() {
        return hotelOrderRepository.findAllByOrderByIdDesc();
    }

    // ======================
    // 🔥 新增：只查询当前用户的订单
    // ======================
    public List<HotelOrder> getOrdersByUsername(String username) {
        return hotelOrderRepository.findByUsernameOrderByIdDesc(username);
    }

    public HotelOrder getById(Long id) {
        Optional<HotelOrder> opt = hotelOrderRepository.findById(id);
        return opt.orElse(null);
    }

    public HotelOrder save(HotelOrder order) {
        return hotelOrderRepository.save(order);
    }
}