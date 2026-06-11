package com.example.ordermanagement.service;

import com.example.ordermanagement.model.RouteScenic;
import com.example.ordermanagement.repository.RouteScenicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteScenicService {

    @Autowired
    private RouteScenicRepository routeScenicRepository;

    public List<RouteScenic> getByRouteId(Long routeId) {
        return routeScenicRepository.findByTourRouteIdOrderByDayNumberAscSortOrderAsc(routeId);
    }

    public List<Long> findRouteIdsByScenicProvince(String province) {
        return routeScenicRepository.findRouteIdsByScenicProvince(province);
    }

    public List<Long> findRouteIdsByScenicCity(String city) {
        return routeScenicRepository.findRouteIdsByScenicCity(city);
    }

    public List<Long> findRouteIdsByScenicProvinceAndCity(String province, String city) {
        return routeScenicRepository.findRouteIdsByScenicProvinceAndCity(province, city);
    }
}
