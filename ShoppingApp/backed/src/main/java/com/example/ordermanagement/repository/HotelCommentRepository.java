package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelCommentRepository extends JpaRepository<HotelComment, Long> {
    List<HotelComment> findByHotelId(Long hotelId);
    boolean existsByOrderId(Long orderId);
}