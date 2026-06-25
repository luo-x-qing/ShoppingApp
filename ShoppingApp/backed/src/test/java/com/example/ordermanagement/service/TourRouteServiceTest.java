package com.example.ordermanagement.service;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.repository.TourRouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourRouteServiceTest {

    @Mock
    private TourRouteRepository tourRouteRepository;

    @InjectMocks
    private TourRouteService tourRouteService;

    private TourRoute createRoute(Long id, String name, Integer days) {
        TourRoute r = new TourRoute();
        r.setId(id);
        r.setName(name);
        r.setDays(days);
        r.setPrice(1000.0);
        return r;
    }

    @Test
    void testList() {
        when(tourRouteRepository.findAll()).thenReturn(Arrays.asList(createRoute(1L, "路线A", 3)));
        List<TourRoute> result = tourRouteService.list();
        assertEquals(1, result.size());
        assertEquals("路线A", result.get(0).getName());
    }

    @Test
    void testSaveNew() {
        TourRoute input = createRoute(null, "新路线", 3);
        when(tourRouteRepository.save(any(TourRoute.class))).thenAnswer(inv -> {
            TourRoute r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        TourRoute result = tourRouteService.save(input);
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());
    }

    @Test
    void testSaveUpdateExisting() {
        TourRoute input = createRoute(1L, "更新路线", 5);
        when(tourRouteRepository.save(any(TourRoute.class))).thenAnswer(inv -> inv.getArgument(0));
        TourRoute result = tourRouteService.save(input);
        assertEquals(1L, result.getId().longValue());
        assertEquals("更新路线", result.getName());
        assertNotNull(result.getUpdateTime());
    }

    @Test
    void testGetByIdFound() {
        TourRoute route = createRoute(1L, "路线A", 3);
        when(tourRouteRepository.findById(1L)).thenReturn(Optional.of(route));
        TourRoute result = tourRouteService.getById(1L);
        assertNotNull(result);
        assertEquals("路线A", result.getName());
    }

    @Test
    void testGetByIdNotFound() {
        when(tourRouteRepository.findById(99L)).thenReturn(Optional.empty());
        TourRoute result = tourRouteService.getById(99L);
        assertNull(result);
    }

    @Test
    void testDelete() {
        tourRouteService.delete(1L);
        verify(tourRouteRepository, times(1)).deleteById(1L);
    }
}
