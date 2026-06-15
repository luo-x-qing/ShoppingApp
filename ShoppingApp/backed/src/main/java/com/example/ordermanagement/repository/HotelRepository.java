package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    List<Hotel> findByCategory(String category);
    
    List<Hotel> findByMerchantId(Long merchantId);
    
    Hotel findByName(String name);
    
    @Query("SELECT h FROM Hotel h WHERE h.status = '营业中' ORDER BY h.price ASC")
    List<Hotel> findAllOrderByPriceAsc();
    
    @Query("SELECT h FROM Hotel h WHERE h.status = '营业中' ORDER BY h.avgRating DESC")
    List<Hotel> findAllOrderByRatingDesc();
    
    @Query("SELECT h FROM Hotel h WHERE h.status = '营业中' ORDER BY " +
           "SQRT(POWER(h.latitude - :lat, 2) + POWER(h.longitude - :lng, 2)) ASC")
    List<Hotel> findAllOrderByDistance(@Param("lat") double lat, @Param("lng") double lng);
    
    @Query("SELECT h FROM Hotel h WHERE h.status = '营业中' " +
           "AND (:categories IS NULL OR h.category IN :categories) " +
           "AND h.price BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY h.avgRating DESC")
    List<Hotel> findRecommendedHotels(@Param("categories") List<String> categories,
                                       @Param("minPrice") double minPrice,
                                       @Param("maxPrice") double maxPrice);

    @Query("SELECT h FROM Hotel h WHERE h.status = '营业中' AND " +
            "(:keyword IS NULL OR " +
            "h.name LIKE CONCAT('%', :keyword, '%') OR " +
            "h.address LIKE CONCAT('%', :keyword, '%') OR " +
            "h.category LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:category IS NULL OR h.category = :category)")
    List<Hotel> searchByKeyword(@Param("keyword") String keyword,
                                @Param("category") String category);
}