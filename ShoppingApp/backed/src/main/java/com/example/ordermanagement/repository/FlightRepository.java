package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    // 按出发城市、到达城市、出发时间范围查询
    List<Flight> findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
        String departCity, String arriveCity, 
        LocalDateTime startTime, LocalDateTime endTime, String status);
    
    // 按出发城市查询
    List<Flight> findByDepartCityAndStatusOrderByPriceAsc(String departCity, String status);
    
    // 按到达城市查询
    List<Flight> findByArriveCityAndStatusOrderByPriceAsc(String arriveCity, String status);
    
    // 按价格排序（升序）
    List<Flight> findByStatusOrderByPriceAsc(String status);
    
    // 按价格排序（降序）
    List<Flight> findByStatusOrderByPriceDesc(String status);
    
    // 按出发时间排序
    List<Flight> findByStatusOrderByDepartTimeAsc(String status);
    
    // 按出发时间+价格排序
    List<Flight> findByStatusOrderByDepartTimeAscPriceAsc(String status);
    
    // 扣减座位
    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.remainingSeats = f.remainingSeats - 1 WHERE f.id = :id AND f.remainingSeats > 0")
    int decreaseSeats(Long id);
    
    // 恢复座位（取消订单时）
    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.remainingSeats = f.remainingSeats + 1 WHERE f.id = :id")
    int increaseSeats(Long id);
    
    // 过期航班处理（出发时间已过）
    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.status = '已失效' WHERE f.departTime < :now AND f.status = '有效'")
    int expireFlights(LocalDateTime now);
}