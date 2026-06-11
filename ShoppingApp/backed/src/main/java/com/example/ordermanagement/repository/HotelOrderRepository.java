package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HotelOrderRepository extends JpaRepository<HotelOrder, Long> {
    List<HotelOrder> findAllByOrderByIdDesc();

    // ======================
    // 🔥 新增：根据用户名查询订单
    // ======================
    List<HotelOrder> findByUsernameOrderByIdDesc(String username);

    Optional<HotelOrder> findById(Long id);
}