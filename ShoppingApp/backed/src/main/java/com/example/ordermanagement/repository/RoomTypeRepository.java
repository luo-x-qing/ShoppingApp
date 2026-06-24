package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    
    List<RoomType> findByHotelId(Long hotelId);
    
    // 扣减房间库存
    @Modifying
    @Transactional
    @Query("UPDATE RoomType rt SET rt.availableCount = rt.availableCount - :count WHERE rt.id = :id AND rt.availableCount >= :count")
    int decreaseAvailableCount(Long id, Integer count);
    
    // 恢复房间库存
    @Modifying
    @Transactional
    @Query("UPDATE RoomType rt SET rt.availableCount = rt.availableCount + :count WHERE rt.id = :id")
    int increaseAvailableCount(Long id, Integer count);
}