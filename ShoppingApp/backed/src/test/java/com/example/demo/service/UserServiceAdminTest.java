package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private User createMerchant(Long id, String username, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("MERCHANT");
        user.setStatus(status);
        user.setShopName("店铺" + username);
        return user;
    }

    private User createNormalUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setPassword("pass123");
        user.setToken("token123");
        return user;
    }

    @Test
    void approveMerchant_whenExists_shouldSetNormal() {
        User merchant = createMerchant(1L, "merchant1", "PENDING");
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.approveMerchant(1L);

        assertTrue(result);
        assertEquals("NORMAL", merchant.getStatus());
        verify(userRepository).save(merchant);
    }

    @Test
    void approveMerchant_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.approveMerchant(999L);

        assertFalse(result);
    }

    @Test
    void approveMerchant_whenNotMerchant_shouldReturnFalse() {
        User user = createNormalUser(1L, "normal_user");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.approveMerchant(1L);

        assertFalse(result);
    }

    @Test
    void rejectMerchant_whenExists_shouldSetRejected() {
        User merchant = createMerchant(1L, "merchant1", "PENDING");
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.rejectMerchant(1L, "资料不全");

        assertTrue(result);
        assertEquals("REJECTED", merchant.getStatus());
    }

    @Test
    void rejectMerchant_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.rejectMerchant(999L, "原因");

        assertFalse(result);
    }

    @Test
    void disableMerchant_whenExists_shouldSetBanned() {
        User merchant = createMerchant(1L, "merchant1", "NORMAL");
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.disableMerchant(1L);

        assertTrue(result);
        assertEquals("BANNED", merchant.getStatus());
    }

    @Test
    void disableMerchant_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.disableMerchant(999L);

        assertFalse(result);
    }

    @Test
    void enableMerchant_whenExists_shouldSetNormal() {
        User merchant = createMerchant(1L, "merchant1", "BANNED");
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.enableMerchant(1L);

        assertTrue(result);
        assertEquals("NORMAL", merchant.getStatus());
    }

    @Test
    void enableMerchant_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.enableMerchant(999L);

        assertFalse(result);
    }

    @Test
    void banUser_shouldSetBannedAndNotify() {
        User user = createNormalUser(1L, "test_user");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.banUser(1L, "违规发言", 7);

        assertTrue(result);
        assertEquals("BANNED", user.getStatus());
        assertEquals("违规发言", user.getBanReason());
        assertNotNull(user.getBanExpireTime());
        verify(notificationService).sendUserBannedNotification(1L, "test_user", "违规发言", 7);
    }

    @Test
    void banUser_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.banUser(999L, "违规发言", 7);

        assertFalse(result);
    }

    @Test
    void unbanUser_shouldSetNormalAndNotify() {
        User user = createNormalUser(1L, "test_user");
        user.setStatus("BANNED");
        user.setBanReason("违规发言");
        user.setBanExpireTime(LocalDateTime.now().plusDays(7));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.unbanUser(1L);

        assertTrue(result);
        assertEquals("NORMAL", user.getStatus());
        assertNull(user.getBanReason());
        assertNull(user.getBanExpireTime());
        verify(notificationService).sendUserUnbannedNotification(1L, "test_user");
    }

    @Test
    void unbanUser_whenNotExists_shouldReturnFalse() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userService.unbanUser(999L);

        assertFalse(result);
    }

    @Test
    void isBanned_shouldReturnTrueWhenBanned() {
        User user = createNormalUser(1L, "test_user");
        user.setStatus("BANNED");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertTrue(userService.isBanned(1L));
    }

    @Test
    void isBanned_shouldReturnFalseWhenNormal() {
        User user = createNormalUser(1L, "test_user");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertFalse(userService.isBanned(1L));
    }

    @Test
    void deleteUser_shouldDeleteById() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
