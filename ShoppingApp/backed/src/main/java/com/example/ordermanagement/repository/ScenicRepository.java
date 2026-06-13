package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Scenic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenicRepository extends JpaRepository<Scenic, Long> {
    List<Scenic> findByProvince(String province);
    List<Scenic> findByCity(String city);
    List<Scenic> findByProvinceAndCity(String province, String city);
    List<Scenic> findByProvinceAndNameContaining(String province, String name);
    List<Scenic> findByCityAndNameContaining(String city, String name);
    List<Scenic> findByNameContaining(String name);
}
