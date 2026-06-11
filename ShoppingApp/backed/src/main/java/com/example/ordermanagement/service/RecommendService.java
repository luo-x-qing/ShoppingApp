package com.example.ordermanagement.service;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.repository.TourRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    @Autowired
    private TourRouteRepository tourRouteRepository;

    @Autowired
    private RouteScenicService routeScenicService;

    public List<TourRoute> recommendByProvince(String province) {
        List<Long> routeIds = routeScenicService.findRouteIdsByScenicProvince(province);
        return sortRoutesByCount(routeIds);
    }

    public List<TourRoute> recommendByCity(String city) {
        List<Long> routeIds = routeScenicService.findRouteIdsByScenicCity(city);
        return sortRoutesByCount(routeIds);
    }

    public List<TourRoute> recommendByProvinceAndCity(String province, String city) {
        List<Long> routeIds = routeScenicService.findRouteIdsByScenicProvinceAndCity(province, city);
        return sortRoutesByCount(routeIds);
    }

    private List<TourRoute> sortRoutesByCount(List<Long> routeIds) {
        if (routeIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<TourRoute> allRoutes = tourRouteRepository.findAllById(routeIds);
        Map<Long, Long> countMap = routeIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        allRoutes.sort((a, b) -> Long.compare(
                countMap.getOrDefault(b.getId(), 0L),
                countMap.getOrDefault(a.getId(), 0L)));
        return allRoutes;
    }

    public List<TourRoute> recommendAll() {
        return tourRouteRepository.findAll();
    }
}
