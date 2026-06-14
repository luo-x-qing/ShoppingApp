package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
        String departCity, String arriveCity,
        LocalDateTime startTime, LocalDateTime endTime, String status);

    List<Flight> findByDepartCityAndStatusOrderByPriceAsc(String departCity, String status);

    List<Flight> findByArriveCityAndStatusOrderByPriceAsc(String arriveCity, String status);

    List<Flight> findByStatusOrderByPriceAsc(String status);

    List<Flight> findByStatusOrderByPriceDesc(String status);

    List<Flight> findByStatusOrderByDepartTimeAsc(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.remainingSeats = f.remainingSeats - 1 WHERE f.id = :id AND f.remainingSeats > 0")
    int decreaseSeats(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.remainingSeats = f.remainingSeats + 1 WHERE f.id = :id")
    int increaseSeats(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Flight f SET f.status = '已失效' WHERE f.departTime < :now AND f.status = '有效'")
    int expireFlights(LocalDateTime now);
}
