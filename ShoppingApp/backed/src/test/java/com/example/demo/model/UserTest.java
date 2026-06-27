package com.example.demo.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructor_shouldSetDefaults() {
        User user = new User();
        assertEquals("USER", user.getRole());
        assertEquals("NORMAL", user.getStatus());
        assertNotNull(user.getCreateTime());
    }

    @Test
    void isMerchant_shouldReturnTrueForMerchantRole() {
        User user = new User();
        user.setRole("MERCHANT");
        assertTrue(user.isMerchant());
        assertFalse(user.isUser());
    }

    @Test
    void isBanned_shouldReturnTrueWhenBanned() {
        User user = new User();
        user.setStatus("BANNED");
        assertTrue(user.isBanned());
    }

    @Test
    void isBanExpired_shouldReturnTrueWhenExpired() {
        User user = new User();
        user.setBanExpireTime(LocalDateTime.now().minusDays(1));
        assertTrue(user.isBanExpired());
    }

    @Test
    void setterAndGetter_shouldWorkCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_user");
        user.setPassword("secret");
        user.setToken("token123");
        user.setRole("MERCHANT");
        user.setStatus("PENDING");
        user.setPhone("13800138000");
        user.setEmail("test@test.com");
        user.setGender("男");
        user.setAge(30);
        user.setAvatar("avatar.jpg");
        user.setBanReason("违规");
        user.setBanExpireTime(LocalDateTime.now().plusDays(7));
        user.setShopName("测试店铺");
        user.setShopDescription("测试描述");
        user.setShopAddress("测试地址");
        user.setShopPhone("13900139000");

        assertEquals(1L, user.getId());
        assertEquals("test_user", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("token123", user.getToken());
        assertEquals("MERCHANT", user.getRole());
        assertEquals("PENDING", user.getStatus());
        assertEquals("13800138000", user.getPhone());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("男", user.getGender());
        assertEquals(Integer.valueOf(30), user.getAge());
        assertEquals("avatar.jpg", user.getAvatar());
        assertEquals("违规", user.getBanReason());
        assertNotNull(user.getBanExpireTime());
        assertEquals("测试店铺", user.getShopName());
        assertEquals("测试描述", user.getShopDescription());
        assertEquals("测试地址", user.getShopAddress());
        assertEquals("13900139000", user.getShopPhone());
    }
}
