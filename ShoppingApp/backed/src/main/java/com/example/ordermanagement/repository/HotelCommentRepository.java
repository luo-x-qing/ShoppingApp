package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface HotelCommentRepository extends JpaRepository<HotelComment, Long> {

    List<HotelComment> findByHotelId(Long hotelId);

    boolean existsByOrderId(Long orderId);

    List<HotelComment> findByStatus(String status);

    // ========== 新增：评分计算方法 ==========
    @Query("SELECT AVG(c.score) FROM HotelComment c WHERE c.hotelId = :hotelId")
    Double getAvgScoreByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(c) FROM HotelComment c WHERE c.hotelId = :hotelId")
    Integer getCommentCountByHotelId(@Param("hotelId") Long hotelId);

    // ========== 商家查询方法 ==========
    @Query("SELECT c FROM HotelComment c WHERE c.hotelId IN :hotelIds")
    List<HotelComment> findByHotelIds(@Param("hotelIds") List<Long> hotelIds);

    @Query("SELECT c FROM HotelComment c WHERE c.hotelId IN :hotelIds AND c.score = :rating")
    List<HotelComment> findByHotelIdsAndRating(@Param("hotelIds") List<Long> hotelIds, @Param("rating") Integer rating);

    @Modifying
    @Transactional
    @Query("UPDATE HotelComment c SET c.status = :status WHERE c.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE HotelComment c SET c.reply = :reply, c.replyTime = CURRENT_TIMESTAMP WHERE c.id = :id")
    int updateReply(@Param("id") Long id, @Param("reply") String reply);
}