package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<TourRoute>> recommendRoutes(
            @RequestParam(required = false, defaultValue = "") String province,
            @RequestParam(required = false, defaultValue = "") String city) {
        if (!city.isEmpty() && !province.isEmpty()) {
            return ResponseEntity.ok(recommendService.recommendByProvinceAndCity(province, city));
        }
        if (!city.isEmpty()) {
            return ResponseEntity.ok(recommendService.recommendByCity(city));
        }
        if (!province.isEmpty()) {
            return ResponseEntity.ok(recommendService.recommendByProvince(province));
        }
        return ResponseEntity.ok(recommendService.recommendAll());
    }
}
