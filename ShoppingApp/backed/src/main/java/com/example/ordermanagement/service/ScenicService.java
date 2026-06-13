package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.repository.ScenicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ScenicService {

    @Autowired
    private ScenicRepository scenicRepository;

    @Autowired
    private AmapService amapService;

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
        autoFillCity(scenic);
        return scenicRepository.save(scenic);
    }

    public Scenic getById(Long id) {
        return scenicRepository.findById(id).orElse(null);
    }

    public Scenic update(Long id, Scenic scenic) {
        Scenic existing = getById(id);
        if (existing == null) return null;
        scenic.setId(id);
        scenic.setCreateTime(existing.getCreateTime());
        scenic.setUpdateTime(LocalDateTime.now());
        autoFillCity(scenic);
        return scenicRepository.save(scenic);
    }

    public void delete(Long id) {
        scenicRepository.deleteById(id);
    }

    public List<Scenic> search(String province, String city, String name) {
        if (name != null && !name.isEmpty()) {
            if (city != null && !city.isEmpty()) {
                return scenicRepository.findByCityAndNameContaining(city, name);
            }
            if (province != null && !province.isEmpty()) {
                return scenicRepository.findByProvinceAndNameContaining(province, name);
            }
            return scenicRepository.findByNameContaining(name);
        }
        if (city != null && !city.isEmpty()) {
            return scenicRepository.findByCity(city);
        }
        if (province != null && !province.isEmpty()) {
            return scenicRepository.findByProvince(province);
        }
        return scenicRepository.findAll();
    }

    private void autoFillCity(Scenic scenic) {
        if ((scenic.getCity() == null || scenic.getCity().isEmpty())
                && scenic.getName() != null && !scenic.getName().isEmpty()) {
            String address = scenic.getName();
            if (scenic.getProvince() != null && !scenic.getProvince().isEmpty()) {
                address = scenic.getName() + "," + scenic.getProvince();
            }
            Map<String, String> detail = amapService.geoDetail(address);
            if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                scenic.setCity(detail.get("city"));
            }
        }
    }
}
