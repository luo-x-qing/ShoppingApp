package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.repository.AttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private AmapService amapService;

    // 查询所有景点
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    // 根据ID查询景点
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id).orElse(null);
    }

    // 按省份查询景点
    public List<Attraction> getAttractionsByProvince(String province) {
        return attractionRepository.findByProvince(province);
    }

    // 按城市查询景点
    public List<Attraction> getAttractionsByCity(String city) {
        return attractionRepository.findByCity(city);
    }

    // 按省份和城市查询景点
    public List<Attraction> getAttractionsByProvinceAndCity(String province, String city) {
        return attractionRepository.findByProvinceAndCity(province, city);
    }

    // 新增/更新景点（自动补全城市）
    public Attraction saveAttraction(Attraction attraction) {
        if ((attraction.getCity() == null || attraction.getCity().isEmpty())
                && attraction.getName() != null && !attraction.getName().isEmpty()) {
            String address = attraction.getName();
            if (attraction.getProvince() != null && !attraction.getProvince().isEmpty()) {
                address = attraction.getName() + "," + attraction.getProvince();
            }
            Map<String, String> detail = amapService.geoDetail(address);
            if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                attraction.setCity(detail.get("city"));
            }
        }
        return attractionRepository.save(attraction);
    }

    // 批量自动补全城市（通过高德API）
    public int autoFillCity() {
        List<Attraction> all = attractionRepository.findAll();
        int count = 0;
        for (Attraction a : all) {
            if (a.getCity() == null || a.getCity().isEmpty()) {
                String address = a.getName();
                if (a.getProvince() != null && !a.getProvince().isEmpty()) {
                    address = a.getName() + "," + a.getProvince();
                }
                Map<String, String> detail = amapService.geoDetail(address);
                if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                    a.setCity(detail.get("city"));
                    attractionRepository.save(a);
                    count++;
                }
            }
        }
        return count;
    }

    // 删除景点
    public void deleteAttraction(Long id) {
        attractionRepository.deleteById(id);
    }

    // 按省份搜索景点
    public List<Attraction> searchByProvinceAndName(String province, String name){
        return attractionRepository.findByProvinceAndNameContaining(province, name);
    }

    // 按城市搜索景点
    public List<Attraction> searchByCityAndName(String city, String name){
        return attractionRepository.findByCityAndNameContaining(city, name);
    }
}