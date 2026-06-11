package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.service.TourRouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tour-route")
public class TourRouteController {

    private final TourRouteService tourRouteService;

    public TourRouteController(TourRouteService tourRouteService) {
        this.tourRouteService = tourRouteService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<TourRoute>> list() {
        return ResponseEntity.ok(tourRouteService.list());
    }

    @PostMapping("/add")
    public ResponseEntity<TourRoute> add(@RequestBody TourRoute tourRoute) {
        tourRoute.setAuditStatus(0);
        return ResponseEntity.ok(tourRouteService.save(tourRoute));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourRoute> getById(@PathVariable Long id) {
        TourRoute tourRoute = tourRouteService.getById(id);
        return tourRoute != null ? ResponseEntity.ok(tourRoute) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<TourRoute> getDetail(@PathVariable Long id) {
        TourRoute tourRoute = tourRouteService.getByIdWithScenics(id);
        return tourRoute != null ? ResponseEntity.ok(tourRoute) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourRoute> update(@PathVariable Long id, @RequestBody TourRoute tourRoute) {
        TourRoute existing = tourRouteService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        tourRoute.setId(id);
        return ResponseEntity.ok(tourRouteService.save(tourRoute));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourRouteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
