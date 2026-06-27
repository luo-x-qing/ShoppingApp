package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelImage;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    private ObjectMapper objectMapper;
    private Hotel sampleHotel;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleHotel = new Hotel();
        sampleHotel.setId(1L);
        sampleHotel.setName("测试酒店");
        sampleHotel.setAddress("北京市朝阳区测试路123号");
        sampleHotel.setCategory("豪华酒店");
        sampleHotel.setStarLevel(5);
        sampleHotel.setPrice(800.0);
        sampleHotel.setMerchantId(10L);
        sampleHotel.setLatitude(39.9042);
        sampleHotel.setLongitude(116.4074);
        sampleHotel.setAvgRating(4.5);
        sampleHotel.setTotalRooms(100);
        sampleHotel.setStatus("营业中");
        sampleHotel.setCoverImage("http://example.com/cover.jpg");
        sampleHotel.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void getHotelsByCategory_ShouldReturnHotels() throws Exception {
        List<Hotel> hotels = Arrays.asList(sampleHotel);
        when(hotelService.getHotelsByCategory("豪华酒店")).thenReturn(hotels);

        mockMvc.perform(get("/api/hotels/category/豪华酒店")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].category").value("豪华酒店"));

        verify(hotelService, times(1)).getHotelsByCategory("豪华酒店");
    }

    @Test
    void getHotelsByCategory_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        when(hotelService.getHotelsByCategory("不存在的分类")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/hotels/category/不存在的分类")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(hotelService, times(1)).getHotelsByCategory("不存在的分类");
    }
}