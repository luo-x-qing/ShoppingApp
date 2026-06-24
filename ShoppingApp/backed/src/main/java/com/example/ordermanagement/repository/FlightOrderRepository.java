package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.FlightOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightOrderRepository extends JpaRepository<FlightOrder, Long> {
    
    // 根据用户名查询订单，按创建时间倒序
    List<FlightOrder> findByUsernameOrderByCreateTimeDesc(String username);
    
    // 根据订单状态查询
    List<FlightOrder> findByStatus(String status);
    
    // 根据用户名和状态查询
    List<FlightOrder> findByUsernameAndStatus(String username, String status);
    
    // 处理未支付的过期订单（超过30分钟未支付）
    @Modifying
    @Transactional
    @Query("UPDATE FlightOrder o SET o.status = '已取消' WHERE o.status = '待支付' AND o.createTime < :expireTime")
    int expireOrders(@Param("expireTime") LocalDateTime expireTime);
    
    // 处理已完成订单（航班已到达）
    @Modifying
    @Transactional
    @Query("UPDATE FlightOrder o SET o.status = '已完成' WHERE o.status = '已出票' AND o.arriveTime < :now")
    int completeOrders(@Param("now") LocalDateTime now);
}