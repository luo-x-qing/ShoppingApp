package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.TourRoute;
import com.example.ordermanagement.service.TourRouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TourRouteControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TourRouteService tourRouteService;

    @InjectMocks
    private TourRouteController tourRouteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tourRouteController).build();
    }

    private TourRoute createRoute(Long id, String name, Integer days) {
        TourRoute r = new TourRoute();
        r.setId(id);
        r.setName(name);
        r.setDays(days);
        r.setPrice(1000.0);
        return r;
    }

    @Test
    void testList() throws Exception {
        List<TourRoute> routes = Arrays.asList(createRoute(1L, "路线A", 3));
        when(tourRouteService.list()).thenReturn(routes);

        mockMvc.perform(get("/tour-route/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("路线A")));
    }

    @Test
    void testAdd() throws Exception {
        TourRoute input = createRoute(null, "新路线", 3);
        TourRoute saved = createRoute(1L, "新路线", 3);
        saved.setAuditStatus(0);
        when(tourRouteService.save(any(TourRoute.class))).thenReturn(saved);

        mockMvc.perform(post("/tour-route/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.auditStatus", is(0)));
    }

    @Test
    void testGetByIdFound() throws Exception {
        TourRoute route = createRoute(1L, "路线A", 3);
        when(tourRouteService.getById(1L)).thenReturn(route);

        mockMvc.perform(get("/tour-route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("路线A")));
    }

    @Test
    void testGetByIdNotFound() throws Exception {
        when(tourRouteService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/tour-route/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDetail() throws Exception {
        TourRoute route = createRoute(1L, "路线A", 3);
        when(tourRouteService.getByIdWithScenics(1L)).thenReturn(route);

        mockMvc.perform(get("/tour-route/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("路线A")));
    }

    @Test
    void testUpdateFound() throws Exception {
        TourRoute existing = createRoute(1L, "旧路线", 3);
        TourRoute updateInput = createRoute(null, "更新路线", 5);
        TourRoute updated = createRoute(1L, "更新路线", 5);

        when(tourRouteService.getById(1L)).thenReturn(existing);
        when(tourRouteService.save(any(TourRoute.class))).thenReturn(updated);

        mockMvc.perform(put("/tour-route/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("更新路线")));
    }

    @Test
    void testUpdateNotFound() throws Exception {
        TourRoute updateInput = createRoute(null, "更新路线", 5);
        when(tourRouteService.getById(99L)).thenReturn(null);

        mockMvc.perform(put("/tour-route/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(tourRouteService).delete(1L);

        mockMvc.perform(delete("/tour-route/1"))
                .andExpect(status().isNoContent());

        verify(tourRouteService, times(1)).delete(1L);
    }
}
