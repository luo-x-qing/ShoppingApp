package com.example.demo.controller;

import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
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
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminUserController adminUserController;

    private User createUser(Long id, String username, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("USER");
        user.setStatus(status);
        user.setPassword("pass123");
        user.setToken("token123");
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    @Test
    void getUsers_shouldReturnPagedList() {
        List<User> allUsers = Arrays.asList(
            createUser(1L, "user1", "NORMAL"),
            createUser(2L, "user2", "NORMAL"),
            createUser(3L, "user3", "BANNED")
        );
        when(userService.getAllNormalUsers()).thenReturn(allUsers);

        Result<Map<String, Object>> result = adminUserController.getUsers(1, 15, null, null);

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertEquals(3L, data.get("total"));
        // Should clear password and token
        List<User> list = (List<User>) data.get("list");
        list.forEach(u -> {
            assertNull(u.getPassword());
            assertNull(u.getToken());
        });
    }

    @Test
    void getUsers_withKeyword_shouldFilter() {
        List<User> allUsers = Arrays.asList(
            createUser(1L, "john", "NORMAL"),
            createUser(2L, "jane", "NORMAL")
        );
        when(userService.getAllNormalUsers()).thenReturn(allUsers);

        Result<Map<String, Object>> result = adminUserController.getUsers(1, 15, "john", null);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().get("total"));
    }

    @Test
    void getUsers_withStatus_shouldFilter() {
        List<User> allUsers = Arrays.asList(
            createUser(1L, "user1", "NORMAL"),
            createUser(2L, "user2", "BANNED")
        );
        when(userService.getAllNormalUsers()).thenReturn(allUsers);

        Result<Map<String, Object>> result = adminUserController.getUsers(1, 15, null, "BANNED");

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().get("total"));
    }

    @Test
    void getUser_whenExists_shouldReturn() {
        User user = createUser(1L, "user1", "NORMAL");
        when(userService.getUserById(1L)).thenReturn(user);

        Result<User> result = adminUserController.getUser(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNull(result.getData().getPassword());
        assertNull(result.getData().getToken());
    }

    @Test
    void getUser_whenNotExists_shouldReturnError() {
        when(userService.getUserById(999L)).thenReturn(null);

        Result<User> result = adminUserController.getUser(999L);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    void banUser_shouldBanAndNotify() {
        User user = createUser(1L, "user1", "NORMAL");
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.banUser(1L, "违规发言", 7)).thenReturn(true);

        Map<String, Object> request = new HashMap<>();
        request.put("reason", "违规发言");
        request.put("durationDays", 7);

        Result<Map<String, Object>> result = adminUserController.banUser(1L, request);

        assertEquals(200, result.getCode());
        verify(userService).banUser(1L, "违规发言", 7);
    }

    @Test
    void banUser_whenNotExists_shouldReturnError() {
        when(userService.getUserById(999L)).thenReturn(null);

        Result<Map<String, Object>> result = adminUserController.banUser(999L, null);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    void unbanUser_shouldUnbanAndNotify() {
        User user = createUser(1L, "user1", "BANNED");
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.unbanUser(1L)).thenReturn(true);

        Result<Map<String, Object>> result = adminUserController.unbanUser(1L);

        assertEquals(200, result.getCode());
        verify(userService).unbanUser(1L);
        verify(notificationService).sendUserUnbannedNotification(1L, "user1");
    }

    @Test
    void unbanUser_whenNotExists_shouldReturnError() {
        when(userService.getUserById(999L)).thenReturn(null);

        Result<Map<String, Object>> result = adminUserController.unbanUser(999L);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    void deleteUser_shouldDelete() {
        Result<Map<String, Object>> result = adminUserController.deleteUser(1L);

        assertEquals(200, result.getCode());
        verify(userService).deleteUser(1L);
    }
}
