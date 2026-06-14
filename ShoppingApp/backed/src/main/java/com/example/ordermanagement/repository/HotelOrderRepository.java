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
import java.util.Optional;

public interface HotelOrderRepository extends JpaRepository<HotelOrder, Long> {
    List<HotelOrder> findAllByOrderByIdDesc();
    List<HotelOrder> findByUsernameOrderByIdDesc(String username);
    Optional<HotelOrder> findById(Long id);
    List<HotelOrder> findByHotelId(Long hotelId);
    List<HotelOrder> findByStatus(String status);

    @Query("SELECT o FROM HotelOrder o WHERE o.hotelId IN :hotelIds ORDER BY o.createTime DESC")
    List<HotelOrder> findByHotelIds(@Param("hotelIds") List<Long> hotelIds);

    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = '已确认', o.confirmTime = :now WHERE o.id = :id AND o.status = '待确认'")
    int confirmOrder(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = :status, o.cancelReason = :reason, o.cancelTime = :now WHERE o.id = :id")
    int cancelOrder(@Param("id") Long id, @Param("status") String status, @Param("reason") String reason, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE HotelOrder o SET o.status = '已失效' WHERE o.status IN ('待支付', '待确认') AND o.checkIn < :today")
    int expireOrders(@Param("today") LocalDate today);

    @Query("SELECT o FROM HotelOrder o WHERE o.merchantId = :merchantId ORDER BY o.id DESC")
    List<HotelOrder> findByMerchantId(@Param("merchantId") Long merchantId);
}