package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.repository.ScenicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScenicService {

    @Autowired
    private ScenicRepository scenicRepository;

    public List<Scenic> list() {
        return scenicRepository.findAll();
    }

    public List<Scenic> getByCity(String city) {
        return scenicRepository.findByCity(city);
    }

    public List<Scenic> getByProvinceAndCity(String province, String city) {
        return scenicRepository.findByProvinceAndCity(province, city);
    }

    public Scenic save(Scenic scenic) {
        if (scenic.getId() == null) {
            scenic.setCreateTime(LocalDateTime.now());
        }
        scenic.setUpdateTime(LocalDateTime.now());
        return scenicRepository.save(scenic);
    }

    public Scenic getById(Long id) {
        return scenicRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        scenicRepository.deleteById(id);
    }
}
