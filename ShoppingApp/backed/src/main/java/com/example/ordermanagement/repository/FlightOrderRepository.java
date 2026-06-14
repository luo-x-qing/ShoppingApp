package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.FlightOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightOrderRepository extends JpaRepository<FlightOrder, Long> {
    List<FlightOrder> findAllByOrderByIdDesc();
    List<FlightOrder> findByUsernameOrderByCreateTimeDesc(String username);

    @Modifying
    @Transactional
    @Query("UPDATE FlightOrder o SET o.status = '已取消' WHERE o.status = '待支付' AND o.createTime < :expireTime")
    int expireOrders(@Param("expireTime") LocalDateTime expireTime);

    @Modifying
    @Transactional
    @Query("UPDATE FlightOrder o SET o.status = '已完成' WHERE o.status = '已出票' AND o.arriveTime < :now")
    int completeOrders(@Param("now") LocalDateTime now);
}