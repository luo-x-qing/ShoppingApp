package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelImage;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.service.BaiduMapService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private BaiduMapService baiduMapService;

    private ObjectMapper objectMapper;
    private Hotel sampleHotel;
    private List<Hotel> sampleHotels;
    private RoomType sampleRoomType;
    private List<RoomType> sampleRoomTypes;

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

        HotelImage image = new HotelImage();
        image.setId(1L);
        image.setImageUrl("http://example.com/detail1.jpg");
        image.setHotel(sampleHotel);
        sampleHotel.getDetailImages().add(image);

        sampleRoomType = new RoomType();
        sampleRoomType.setId(1L);
        sampleRoomType.setTypeName("豪华大床房");
        sampleRoomType.setPrice(800.0);
        sampleRoomType.setAvailableCount(10);
        sampleRoomType.setTotalCount(10);
        sampleRoomType.setHotel(sampleHotel);
        sampleRoomTypes = Arrays.asList(sampleRoomType);

        sampleHotels = Arrays.asList(sampleHotel);
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("测试酒店"));

        verify(hotelService, times(1)).getAllHotels();
    }

    @Test
    void getHotelById_WithValidId_ShouldReturnHotel() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(sampleHotel);

        mockMvc.perform(get("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("测试酒店"))
                .andExpect(jsonPath("$.data.merchantId").value(10L));

        verify(hotelService, times(1)).getHotelById(1L);
    }

    @Test
    void getHotelById_WithInvalidId_ShouldReturnError() throws Exception {
        when(hotelService.getHotelById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/hotels/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("酒店不存在"));

        verify(hotelService, times(1)).getHotelById(999L);
    }

    @Test
    void getHotelDetail_ShouldReturnHotelWithRoomTypes() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(sampleHotel);
        when(hotelService.getRoomTypesByHotel(1L)).thenReturn(sampleRoomTypes);

        mockMvc.perform(get("/api/hotels/1/detail")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hotel.id").value(1L))
                .andExpect(jsonPath("$.data.roomTypes").isArray())
                .andExpect(jsonPath("$.data.roomTypes[0].typeName").value("豪华大床房"));

        verify(hotelService, times(1)).getHotelById(1L);
        verify(hotelService, times(1)).getRoomTypesByHotel(1L);
    }

    @Test
    void saveHotel_ShouldCreateHotel() throws Exception {
        when(hotelService.saveHotel(any(Hotel.class))).thenReturn(sampleHotel);

        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleHotel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("测试酒店"));

        verify(hotelService, times(1)).saveHotel(any(Hotel.class));
    }

    @Test
    void updateHotel_WithValidId_ShouldUpdateHotel() throws Exception {
        Hotel updatedHotel = new Hotel();
        updatedHotel.setName("更新后的酒店");
        updatedHotel.setCategory("商务酒店");
        updatedHotel.setAddress("上海市浦东新区测试路456号");
        updatedHotel.setStarLevel(4);
        updatedHotel.setPrice(600.0);

        when(hotelService.getHotelById(1L)).thenReturn(sampleHotel);
        when(hotelService.saveHotel(any(Hotel.class))).thenReturn(sampleHotel);

        mockMvc.perform(put("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedHotel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(hotelService, times(1)).getHotelById(1L);
        verify(hotelService, times(1)).saveHotel(any(Hotel.class));
    }

    @Test
    void updateHotel_WithInvalidId_ShouldReturnError() throws Exception {
        when(hotelService.getHotelById(999L)).thenReturn(null);

        mockMvc.perform(put("/api/hotels/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleHotel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("酒店不存在"));

        verify(hotelService, never()).saveHotel(any(Hotel.class));
    }

    @Test
    void deleteHotel_ShouldDeleteHotel() throws Exception {
        doNothing().when(hotelService).deleteHotel(1L);

        mockMvc.perform(delete("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelService, times(1)).deleteHotel(1L);
    }

    @Test
    void getHotelsByCategory_ShouldReturnHotels() throws Exception {
        when(hotelService.getHotelsByCategory("豪华酒店")).thenReturn(sampleHotels);

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
        when(hotelService.getHotelsByCategory("不存在的分类")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/hotels/category/不存在的分类")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(hotelService, times(1)).getHotelsByCategory("不存在的分类");
    }

    @Test
    void getHotelsByCategory_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        when(hotelService.getHotelsByCategory(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/hotels/category/豪华酒店")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询失败：Database connection failed"));

        verify(hotelService, times(1)).getHotelsByCategory(anyString());
    }

    @Test
    void sortByPrice_ShouldReturnSortedHotels() throws Exception {
        when(hotelService.getHotelsOrderByPrice()).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/sort/price")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).getHotelsOrderByPrice();
    }

    @Test
    void sortByRating_ShouldReturnSortedHotels() throws Exception {
        when(hotelService.getHotelsOrderByRating()).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/sort/rating")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).getHotelsOrderByRating();
    }

    @Test
    void sortByDistance_ShouldReturnHotelsByDistance() throws Exception {
        when(hotelService.getHotelsOrderByDistance(39.9042, 116.4074)).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/sort/distance")
                .param("lat", "39.9042")
                .param("lng", "116.4074")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).getHotelsOrderByDistance(39.9042, 116.4074);
    }

    @Test
    void searchHotels_WithKeyword_ShouldReturnMatchingHotels() throws Exception {
        when(hotelService.searchHotels("测试", null)).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/search")
                .param("keyword", "测试")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).searchHotels("测试", null);
    }

    @Test
    void getHotelsByMerchant_ShouldReturnMerchantHotels() throws Exception {
        when(hotelService.getHotelsByMerchant(10L)).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/merchant/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].merchantId").value(10L));

        verify(hotelService, times(1)).getHotelsByMerchant(10L);
    }

    @Test
    void getRecommendations_ShouldReturnRecommendations() throws Exception {
        when(hotelService.getPersonalizedRecommendations("testuser")).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/recommend")
                .param("username", "testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).getPersonalizedRecommendations("testuser");
    }

    @Test
    void getRoomTypes_ShouldReturnRoomTypes() throws Exception {
        when(hotelService.getRoomTypesByHotel(1L)).thenReturn(sampleRoomTypes);

        mockMvc.perform(get("/api/hotels/1/room-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].typeName").value("豪华大床房"));

        verify(hotelService, times(1)).getRoomTypesByHotel(1L);
    }

    @Test
    void addRoomType_ShouldCreateRoomType() throws Exception {
        when(hotelService.addRoomType(any(RoomType.class))).thenReturn(sampleRoomType);

        mockMvc.perform(post("/api/hotels/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRoomType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.typeName").value("豪华大床房"));

        verify(hotelService, times(1)).addRoomType(any(RoomType.class));
    }

    @Test
    void updateRoomType_ShouldUpdateRoomType() throws Exception {
        when(hotelService.updateRoomType(any(RoomType.class))).thenReturn(sampleRoomType);

        mockMvc.perform(put("/api/hotels/room-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRoomType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(hotelService, times(1)).updateRoomType(any(RoomType.class));
    }

    @Test
    void deleteRoomType_ShouldDeleteRoomType() throws Exception {
        doNothing().when(hotelService).deleteRoomType(1L);

        mockMvc.perform(delete("/api/hotels/room-types/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelService, times(1)).deleteRoomType(1L);
    }

    @Test
    void getNearbyHotelsByAddress_ShouldReturnHotels() throws Exception {
        when(hotelService.getHotelsSortedByAddress("北京市朝阳区")).thenReturn(sampleHotels);

        mockMvc.perform(get("/api/hotels/nearby-by-address")
                .param("address", "北京市朝阳区")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(hotelService, times(1)).getHotelsSortedByAddress("北京市朝阳区");
    }

    @Test
    void recognizeAddress_ShouldReturnRecognizedAddress() throws Exception {
        when(hotelService.smartAddressRecognize("北京市朝阳区测试路123号"))
                .thenReturn("北京市朝阳区测试路123号");

        mockMvc.perform(get("/api/hotels/address-recognize")
                .param("address", "北京市朝阳区测试路123号")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.original").value("北京市朝阳区测试路123号"))
                .andExpect(jsonPath("$.data.recognized").value("北京市朝阳区测试路123号"));

        verify(hotelService, times(1)).smartAddressRecognize("北京市朝阳区测试路123号");
    }

    @Test
    void getAllHotels_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        when(hotelService.getAllHotels()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询失败：Database error"));
    }

    @Test
    void saveHotel_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        when(hotelService.saveHotel(any(Hotel.class)))
                .thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleHotel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("保存失败：Save failed"));
    }
}