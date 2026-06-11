package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.TourRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourRouteRepository extends JpaRepository<TourRoute, Long> {
    List<TourRoute> findByMerchantId(Long merchantId);
}
