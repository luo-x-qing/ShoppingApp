package com.example.ordermanagement.service;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.repository.TourRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TourRouteService {

    @Autowired
    private TourRouteRepository tourRouteRepository;

    public List<TourRoute> list() {
        return tourRouteRepository.findAll();
    }

    public TourRoute save(TourRoute tourRoute) {
        if (tourRoute.getId() == null) {
            tourRoute.setCreateTime(LocalDateTime.now());
        }
        tourRoute.setUpdateTime(LocalDateTime.now());
        return tourRouteRepository.save(tourRoute);
    }

    public TourRoute getById(Long id) {
        return tourRouteRepository.findById(id).orElse(null);
    }

    @Transactional
    public TourRoute getByIdWithScenics(Long id) {
        TourRoute route = tourRouteRepository.findById(id).orElse(null);
        if (route != null) {
            route.getRouteScenics().size();
        }
        return route;
    }

    public void delete(Long id) {
        tourRouteRepository.deleteById(id);
    }
}
