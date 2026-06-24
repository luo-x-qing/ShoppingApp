package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HotelOrderRepository extends JpaRepository<HotelOrder, Long> {
    
    List<HotelOrder> findAllByOrderByIdDesc();
    List<HotelOrder> findByUsernameOrderByIdDesc(String username);
    List<HotelOrder> findByHotelId(Long hotelId);
    List<HotelOrder> findByStatus(String status);
    
    // 商家查询自己酒店的订单
    @Query("SELECT o FROM HotelOrder o WHERE o.hotelId IN :hotelIds ORDER BY o.createTime DESC")
    List<HotelOrder> findByHotelIds(List<Long> hotelIds);
    
    // 确认订单
    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = '已确认', o.confirmTime = :now WHERE o.id = :id AND o.status = '待确认'")
    int confirmOrder(Long id, LocalDateTime now);
    
    // 取消订单
    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = :status, o.cancelReason = :reason, o.cancelTime = :now WHERE o.id = :id")
    int cancelOrder(Long id, String status, String reason, LocalDateTime now);
    
    // 过期失效
    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = '已失效' WHERE o.status IN ('待支付', '待确认') AND o.checkIn < :today")
    int expireOrders(LocalDate today);

    // 根据商家ID查询订单
    @Query("SELECT o FROM HotelOrder o WHERE o.merchantId = :merchantId ORDER BY o.id DESC")
    List<HotelOrder> findByMerchantId(@Param("merchantId") Long merchantId);
}