package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Flight;
import com.example.ordermanagement.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight createMockFlight(Long id, String flightNumber, String depart, String arrive, double price) {
        Flight flight = new Flight();
        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setDepartCity(depart);
        flight.setArriveCity(arrive);
        flight.setPrice(price);
        flight.setStatus("有效");
        flight.setRemainingSeats(100);
        return flight;
    }

    @Test
    void searchFlights_shouldDelegateToRepository() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0),
            createMockFlight(2L, "MU5678", "北京", "上海", 900.0)
        );
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 2, 0, 0);

        when(flightRepository.findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
            "北京", "上海", start, end, "有效")).thenReturn(expected);

        List<Flight> result = flightService.searchFlights("北京", "上海", start, end);

        assertEquals(2, result.size());
        assertEquals("CA1234", result.get(0).getFlightNumber());
        verify(flightRepository).findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
            "北京", "上海", start, end, "有效");
    }

    @Test
    void searchFlights_whenNoResults_shouldReturnEmptyList() {
        when(flightRepository.findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
            anyString(), anyString(), any(), any(), anyString())).thenReturn(Collections.emptyList());

        List<Flight> result = flightService.searchFlights("北京", "三亚",
            LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertTrue(result.isEmpty());
    }

    @Test
    void getFlightsOrderByPriceAsc_shouldReturnSortedFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "MU5678", "北京", "上海", 900.0),
            createMockFlight(2L, "CA1234", "北京", "上海", 1200.0)
        );
        when(flightRepository.findByStatusOrderByPriceAsc("有效")).thenReturn(expected);

        List<Flight> result = flightService.getFlightsOrderByPriceAsc();

        assertEquals(2, result.size());
        assertEquals(900.0, result.get(0).getPrice());
        assertEquals(1200.0, result.get(1).getPrice());
    }

    @Test
    void getFlightsOrderByPriceDesc_shouldReturnSortedFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0),
            createMockFlight(2L, "MU5678", "北京", "上海", 900.0)
        );
        when(flightRepository.findByStatusOrderByPriceDesc("有效")).thenReturn(expected);

        List<Flight> result = flightService.getFlightsOrderByPriceDesc();

        assertEquals(2, result.size());
        assertEquals(1200.0, result.get(0).getPrice());
    }

    @Test
    void getFlightsOrderByTime_shouldReturnSortedFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0),
            createMockFlight(2L, "MU5678", "北京", "上海", 900.0)
        );
        when(flightRepository.findByStatusOrderByDepartTimeAsc("有效")).thenReturn(expected);

        List<Flight> result = flightService.getFlightsOrderByTime();

        assertEquals(2, result.size());
    }

    @Test
    void getFlightsByDepartCity_shouldReturnFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0)
        );
        when(flightRepository.findByDepartCityAndStatusOrderByPriceAsc("北京", "有效")).thenReturn(expected);

        List<Flight> result = flightService.getFlightsByDepartCity("北京");

        assertEquals(1, result.size());
        assertEquals("北京", result.get(0).getDepartCity());
    }

    @Test
    void getFlightsByArriveCity_shouldReturnFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0)
        );
        when(flightRepository.findByArriveCityAndStatusOrderByPriceAsc("上海", "有效")).thenReturn(expected);

        List<Flight> result = flightService.getFlightsByArriveCity("上海");

        assertEquals(1, result.size());
        assertEquals("上海", result.get(0).getArriveCity());
    }

    @Test
    void getFlightById_whenExists_shouldReturnFlight() {
        Flight expected = createMockFlight(1L, "CA1234", "北京", "上海", 1200.0);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(expected));

        Flight result = flightService.getFlightById(1L);

        assertNotNull(result);
        assertEquals("CA1234", result.getFlightNumber());
    }

    @Test
    void getFlightById_whenNotExists_shouldReturnNull() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        Flight result = flightService.getFlightById(999L);

        assertNull(result);
    }

    @Test
    void addFlight_shouldSetStatusAndSave() {
        Flight input = createMockFlight(null, "CA1234", "北京", "上海", 1200.0);
        input.setStatus(null);
        Flight saved = createMockFlight(1L, "CA1234", "北京", "上海", 1200.0);

        when(flightRepository.save(any(Flight.class))).thenReturn(saved);

        Flight result = flightService.addFlight(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("有效", saved.getStatus());
        verify(flightRepository).save(input);
    }

    @Test
    void updateFlight_shouldSaveAndReturn() {
        Flight flight = createMockFlight(1L, "CA1234", "北京", "上海", 1500.0);
        when(flightRepository.save(flight)).thenReturn(flight);

        Flight result = flightService.updateFlight(flight);

        assertEquals(1500.0, result.getPrice());
        verify(flightRepository).save(flight);
    }

    @Test
    void deleteFlight_shouldDeleteById() {
        flightService.deleteFlight(1L);

        verify(flightRepository).deleteById(1L);
    }

    @Test
    void getAllFlights_shouldReturnAllFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0),
            createMockFlight(2L, "MU5678", "深圳", "成都", 900.0)
        );
        when(flightRepository.findAll()).thenReturn(expected);

        List<Flight> result = flightService.getAllFlights();

        assertEquals(2, result.size());
    }

    @Test
    void getValidFlights_shouldReturnOnlyValidFlights() {
        List<Flight> expected = Arrays.asList(
            createMockFlight(1L, "CA1234", "北京", "上海", 1200.0)
        );
        when(flightRepository.findByStatusOrderByDepartTimeAsc("有效")).thenReturn(expected);

        List<Flight> result = flightService.getValidFlights();

        assertEquals(1, result.size());
    }

    @Test
    void decreaseSeats_whenSuccessful_shouldReturnTrue() {
        when(flightRepository.decreaseSeats(1L)).thenReturn(1);

        boolean result = flightService.decreaseSeats(1L);

        assertTrue(result);
        verify(flightRepository).decreaseSeats(1L);
    }

    @Test
    void decreaseSeats_whenNoSeats_shouldReturnFalse() {
        when(flightRepository.decreaseSeats(1L)).thenReturn(0);

        boolean result = flightService.decreaseSeats(1L);

        assertFalse(result);
    }

    @Test
    void increaseSeats_whenSuccessful_shouldReturnTrue() {
        when(flightRepository.increaseSeats(1L)).thenReturn(1);

        boolean result = flightService.increaseSeats(1L);

        assertTrue(result);
        verify(flightRepository).increaseSeats(1L);
    }

    @Test
    void increaseSeats_whenFailed_shouldReturnFalse() {
        when(flightRepository.increaseSeats(1L)).thenReturn(0);

        boolean result = flightService.increaseSeats(1L);

        assertFalse(result);
    }
}
