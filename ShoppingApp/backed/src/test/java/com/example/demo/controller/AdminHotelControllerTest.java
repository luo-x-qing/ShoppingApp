package com.example.demo.controller;

import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.service.HotelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminHotelControllerTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminHotelController adminHotelController;

    private Hotel createHotel(Long id, String name, String status, Long merchantId) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setName(name);
        hotel.setStatus(status);
        hotel.setMerchantId(merchantId);
        hotel.setUpdateTime(LocalDateTime.now());
        return hotel;
    }

    @Test
    void getStatistics_shouldReturnCounts() {
        List<Hotel> allHotels = Arrays.asList(
            createHotel(1L, "酒店A", "PENDING", 1L),
            createHotel(2L, "酒店B", "APPROVED", 1L),
            createHotel(3L, "酒店C", "营业中", 2L),
            createHotel(4L, "酒店D", "VIOLATION", 3L),
            createHotel(5L, "酒店E", "违规", 3L),
            createHotel(6L, "酒店F", "SUSPENDED", 4L),
            createHotel(7L, "酒店G", "已停业", 4L)
        );
        when(hotelService.getAllHotels()).thenReturn(allHotels);

        Result<Map<String, Long>> result = adminHotelController.getStatistics();

        assertEquals(200, result.getCode());
        Map<String, Long> data = result.getData();
        assertEquals(7L, data.get("total"));
        assertEquals(1L, data.get("pending"));
        assertEquals(2L, data.get("approved"));
        assertEquals(2L, data.get("violation"));
        assertEquals(2L, data.get("suspended"));
    }

    @Test
    void getTodoList_shouldReturnPendingAndViolation() {
        List<Hotel> allHotels = Arrays.asList(
            createHotel(1L, "酒店A", "PENDING", 1L),
            createHotel(2L, "酒店B", "VIOLATION", 2L)
        );
        when(hotelService.getAllHotels()).thenReturn(allHotels);

        Result<List<Map<String, String>>> result = adminHotelController.getTodoList();

        assertEquals(200, result.getCode());
        List<Map<String, String>> todoList = result.getData();
        assertEquals(2, todoList.size());
        assertTrue(todoList.get(0).get("content").contains("待审核"));
        assertTrue(todoList.get(1).get("content").contains("违规"));
    }

    @Test
    void getTodoList_whenNoTodos_shouldReturnEmptyList() {
        when(hotelService.getAllHotels()).thenReturn(Collections.emptyList());

        Result<List<Map<String, String>>> result = adminHotelController.getTodoList();

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void getRecentViolations_shouldReturnViolationHotels() {
        Hotel violationHotel = createHotel(1L, "违规酒店", "VIOLATION", 1L);
        violationHotel.setViolationReason("虚假宣传");
        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(violationHotel));

        Result<List<Map<String, String>>> result = adminHotelController.getRecentViolations();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("虚假宣传", result.getData().get(0).get("reason"));
    }

    @Test
    void getAllHotels_shouldReturnAll() {
        List<Hotel> expected = Arrays.asList(
            createHotel(1L, "酒店A", "APPROVED", 1L)
        );
        when(hotelService.getAllHotels()).thenReturn(expected);

        Result<List<Hotel>> result = adminHotelController.getAllHotels();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void getHotel_whenExists_shouldReturn() {
        Hotel hotel = createHotel(1L, "酒店A", "APPROVED", 1L);
        when(hotelService.getHotelById(1L)).thenReturn(hotel);

        Result<Hotel> result = adminHotelController.getHotel(1L);

        assertEquals(200, result.getCode());
        assertEquals("酒店A", result.getData().getName());
    }

    @Test
    void getHotel_whenNotExists_shouldReturnError() {
        when(hotelService.getHotelById(999L)).thenReturn(null);

        Result<Hotel> result = adminHotelController.getHotel(999L);

        assertEquals(500, result.getCode());
        assertEquals("酒店不存在", result.getMessage());
    }

    @Test
    void disableHotel_shouldUpdateStatusAndNotify() {
        Hotel hotel = createHotel(1L, "酒店A", "APPROVED", 1L);
        User merchant = new User();
        merchant.setId(1L);
        merchant.setUsername("merchant1");
        merchant.setShopName("店铺1");

        when(hotelService.getHotelById(1L)).thenReturn(hotel);
        when(userService.getUserById(1L)).thenReturn(merchant);

        Map<String, String> request = new HashMap<>();
        request.put("reason", "违规经营");

        Result<Map<String, Object>> result = adminHotelController.disableHotel(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("已停业", hotel.getStatus());
        verify(hotelService).saveHotel(hotel);
        verify(notificationService).sendHotelBannedNotification(1L, "店铺1", 1L, "酒店A", "违规经营");
    }

    @Test
    void disableHotel_whenNotExists_shouldReturnError() {
        when(hotelService.getHotelById(999L)).thenReturn(null);

        Result<Map<String, Object>> result = adminHotelController.disableHotel(999L, null);

        assertEquals(500, result.getCode());
        assertEquals("酒店不存在", result.getMessage());
    }

    @Test
    void enableHotel_shouldUpdateStatusAndNotify() {
        Hotel hotel = createHotel(1L, "酒店A", "已停业", 1L);
        User merchant = new User();
        merchant.setId(1L);
        merchant.setUsername("merchant1");
        merchant.setShopName("店铺1");

        when(hotelService.getHotelById(1L)).thenReturn(hotel);
        when(userService.getUserById(1L)).thenReturn(merchant);

        Result<Map<String, Object>> result = adminHotelController.enableHotel(1L);

        assertEquals(200, result.getCode());
        assertEquals("营业中", hotel.getStatus());
        verify(hotelService).saveHotel(hotel);
        verify(notificationService).sendHotelUnbannedNotification(1L, "店铺1", 1L, "酒店A");
    }

    @Test
    void enableHotel_whenNotExists_shouldReturnError() {
        when(hotelService.getHotelById(999L)).thenReturn(null);

        Result<Map<String, Object>> result = adminHotelController.enableHotel(999L);

        assertEquals(500, result.getCode());
        assertEquals("酒店不存在", result.getMessage());
    }

    @Test
    void deleteHotel_shouldDelete() {
        Result<Map<String, Object>> result = adminHotelController.deleteHotel(1L);

        assertEquals(200, result.getCode());
        verify(hotelService).deleteHotel(1L);
    }

    @Test
    void deleteHotel_whenException_shouldReturnError() {
        doThrow(new RuntimeException("删除失败")).when(hotelService).deleteHotel(1L);

        Result<Map<String, Object>> result = adminHotelController.deleteHotel(1L);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("删除失败"));
    }
}
