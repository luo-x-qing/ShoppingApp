package com.example.ordermanagement.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    @Test
    void prePersist_shouldSetDefaultValues() {
        Flight flight = new Flight();
        flight.prePersist();
        assertEquals("有效", flight.getStatus());
        assertEquals(Integer.valueOf(100), flight.getRemainingSeats());
    }

    @Test
    void prePersist_shouldNotOverrideExistingValues() {
        Flight flight = new Flight();
        flight.setStatus("已失效");
        flight.setRemainingSeats(50);
        flight.prePersist();
        assertEquals("已失效", flight.getStatus());
        assertEquals(Integer.valueOf(50), flight.getRemainingSeats());
    }

    @Test
    void setterAndGetter_shouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(2);

        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("CA1234");
        flight.setAirline("中国国航");
        flight.setDepartCity("北京");
        flight.setArriveCity("上海");
        flight.setDepartTime(now);
        flight.setArriveTime(later);
        flight.setPrice(1200.0);
        flight.setRemainingSeats(80);
        flight.setStatus("有效");

        assertEquals(1L, flight.getId());
        assertEquals("CA1234", flight.getFlightNumber());
        assertEquals("中国国航", flight.getAirline());
        assertEquals("北京", flight.getDepartCity());
        assertEquals("上海", flight.getArriveCity());
        assertEquals(now, flight.getDepartTime());
        assertEquals(later, flight.getArriveTime());
        assertEquals(1200.0, flight.getPrice());
        assertEquals(Integer.valueOf(80), flight.getRemainingSeats());
        assertEquals("有效", flight.getStatus());
    }
}
