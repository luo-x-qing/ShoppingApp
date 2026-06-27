package com.example.ordermanagement.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FlightOrderTest {

    @Test
    void prePersist_shouldSetDefaultValues() {
        FlightOrder order = new FlightOrder();
        order.prePersist();
        assertEquals("待支付", order.getStatus());
        assertNotNull(order.getCreateTime());
    }

    @Test
    void prePersist_shouldNotOverrideExistingStatus() {
        FlightOrder order = new FlightOrder();
        order.setStatus("已取消");
        order.prePersist();
        assertEquals("已取消", order.getStatus());
        assertNotNull(order.getCreateTime());
    }

    @Test
    void setterAndGetter_shouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        FlightOrder order = new FlightOrder();
        order.setId(1L);
        order.setFlightId(10L);
        order.setFlightNumber("MU5678");
        order.setDepartCity("深圳");
        order.setArriveCity("成都");
        order.setDepartTime(now);
        order.setArriveTime(now.plusHours(3));
        order.setPrice(900.0);
        order.setUsername("test_user");
        order.setPassengerName("张三");
        order.setPassengerIdCard("110101199001011234");
        order.setContactPhone("13800138000");
        order.setStatus("待支付");
        order.setCreateTime(now);
        order.setPayTime(now.plusMinutes(15));
        order.setCancelTime(null);

        assertEquals(1L, order.getId());
        assertEquals(10L, order.getFlightId());
        assertEquals("MU5678", order.getFlightNumber());
        assertEquals("深圳", order.getDepartCity());
        assertEquals("成都", order.getArriveCity());
        assertEquals(now, order.getDepartTime());
        assertEquals(now.plusHours(3), order.getArriveTime());
        assertEquals(900.0, order.getPrice());
        assertEquals("test_user", order.getUsername());
        assertEquals("张三", order.getPassengerName());
        assertEquals("110101199001011234", order.getPassengerIdCard());
        assertEquals("13800138000", order.getContactPhone());
        assertEquals("待支付", order.getStatus());
        assertEquals(now, order.getCreateTime());
        assertEquals(now.plusMinutes(15), order.getPayTime());
        assertNull(order.getCancelTime());
    }
}
