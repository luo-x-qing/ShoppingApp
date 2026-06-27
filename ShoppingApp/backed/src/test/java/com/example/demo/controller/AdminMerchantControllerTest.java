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
class AdminMerchantControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private AdminMerchantController adminMerchantController;

    private User createMerchant(Long id, String username, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("MERCHANT");
        user.setStatus(status);
        user.setShopName("店铺" + username);
        user.setPhone("13800138" + String.format("%02d", id));
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    @Test
    void getMerchants_shouldReturnPagedList() {
        List<User> allMerchants = Arrays.asList(
            createMerchant(1L, "merchant1", "PENDING"),
            createMerchant(2L, "merchant2", "NORMAL"),
            createMerchant(3L, "merchant3", "BANNED")
        );
        when(userService.getAllMerchants()).thenReturn(allMerchants);

        Result<Map<String, Object>> result = adminMerchantController.getMerchants(1, 15, null, null);

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertEquals(3L, data.get("total"));
        List<User> list = (List<User>) data.get("list");
        assertEquals(3, list.size());
    }

    @Test
    void getMerchants_withKeyword_shouldFilter() {
        List<User> allMerchants = Arrays.asList(
            createMerchant(1L, "merchant1", "PENDING"),
            createMerchant(2L, "other", "NORMAL")
        );
        when(userService.getAllMerchants()).thenReturn(allMerchants);

        Result<Map<String, Object>> result = adminMerchantController.getMerchants(1, 15, "merchant", null);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().get("total"));
    }

    @Test
    void getMerchant_whenExists_shouldReturn() {
        User merchant = createMerchant(1L, "merchant1", "NORMAL");
        when(userService.getUserById(1L)).thenReturn(merchant);

        Result<User> result = adminMerchantController.getMerchant(1L);

        assertEquals(200, result.getCode());
        assertEquals("merchant1", result.getData().getUsername());
    }

    @Test
    void getMerchant_whenNotMerchant_shouldReturnError() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        when(userService.getUserById(1L)).thenReturn(user);

        Result<User> result = adminMerchantController.getMerchant(1L);

        assertEquals(500, result.getCode());
        assertEquals("商家不存在", result.getMessage());
    }

    @Test
    void reviewMerchant_approve_shouldSucceed() {
        User merchant = createMerchant(1L, "merchant1", "PENDING");
        when(userService.getUserEntity(1L)).thenReturn(merchant);
        when(userService.approveMerchant(1L)).thenReturn(true);

        Map<String, Object> request = new HashMap<>();
        request.put("approved", true);

        Result<Map<String, Object>> result = adminMerchantController.reviewMerchant(1L, request);

        assertEquals(200, result.getCode());
        verify(userService).approveMerchant(1L);
    }

    @Test
    void reviewMerchant_reject_shouldSucceed() {
        User merchant = createMerchant(1L, "merchant1", "PENDING");
        when(userService.getUserEntity(1L)).thenReturn(merchant);
        when(userService.rejectMerchant(1L, "资料不全")).thenReturn(true);

        Map<String, Object> request = new HashMap<>();
        request.put("approved", false);
        request.put("reason", "资料不全");

        Result<Map<String, Object>> result = adminMerchantController.reviewMerchant(1L, request);

        assertEquals(200, result.getCode());
        verify(userService).rejectMerchant(1L, "资料不全");
    }

    @Test
    void reviewMerchant_whenNotExists_shouldReturnError() {
        when(userService.getUserEntity(999L)).thenReturn(null);

        Map<String, Object> request = new HashMap<>();
        request.put("approved", true);

        Result<Map<String, Object>> result = adminMerchantController.reviewMerchant(999L, request);

        assertEquals(500, result.getCode());
        assertEquals("商家不存在", result.getMessage());
    }

    @Test
    void disableMerchant_shouldDisableAndNotify() {
        User merchant = createMerchant(1L, "merchant1", "NORMAL");
        merchant.setPassword("pass123");
        when(userService.getUserEntity(1L)).thenReturn(merchant);
        when(userService.disableMerchant(1L)).thenReturn(true);
        when(hotelService.getHotelsByMerchant(1L)).thenReturn(Collections.emptyList());

        Map<String, String> request = new HashMap<>();
        request.put("reason", "违规经营");

        Result<Map<String, Object>> result = adminMerchantController.disableMerchant(1L, request);

        assertEquals(200, result.getCode());
        assertTrue((Boolean) result.getData().get("success"));
        verify(userService).disableMerchant(1L);
        verify(notificationService).sendMerchantBannedNotification(1L, "店铺merchant1", "违规经营");
    }

    @Test
    void disableMerchant_whenNotExists_shouldReturnError() {
        when(userService.getUserEntity(999L)).thenReturn(null);

        Result<Map<String, Object>> result = adminMerchantController.disableMerchant(999L, null);

        assertEquals(500, result.getCode());
        assertEquals("商家不存在", result.getMessage());
    }

    @Test
    void enableMerchant_shouldEnableAndNotify() {
        User merchant = createMerchant(1L, "merchant1", "BANNED");
        merchant.setPassword("pass123");
        when(userService.getUserEntity(1L)).thenReturn(merchant);
        when(userService.enableMerchant(1L)).thenReturn(true);
        when(hotelService.getHotelsByMerchant(1L)).thenReturn(Collections.emptyList());

        Result<Map<String, Object>> result = adminMerchantController.enableMerchant(1L);

        assertEquals(200, result.getCode());
        verify(notificationService).sendMerchantUnbannedNotification(1L, "店铺merchant1");
    }

    @Test
    void deleteMerchant_shouldDelete() {
        Result<Map<String, Object>> result = adminMerchantController.deleteMerchant(1L);

        assertEquals(200, result.getCode());
        verify(userService).deleteUser(1L);
    }
}
