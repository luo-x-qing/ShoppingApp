package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    // 按省份查询景点
    List<Attraction> findByProvince(String province);
    // 按城市查询景点
    List<Attraction> findByCity(String city);
    // 按省份和城市查询景点
    List<Attraction> findByProvinceAndCity(String province, String city);
    List<Attraction> findByProvinceAndNameContaining(String province, String name);
    List<Attraction> findByCityAndNameContaining(String city, String name);
}