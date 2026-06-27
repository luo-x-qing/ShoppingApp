package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.McpProxyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchControllerTest {

    @Mock
    private McpProxyService mcpProxyService;

    @InjectMocks
    private FlightSearchController flightSearchController;

    @Test
    void searchFlights_whenSuccessful_shouldReturnSuccessTrue() {
        String mockResponse = "{\"flights\":[{\"flightNo\":\"CA1234\"}]}";
        when(mcpProxyService.searchFlights("北京", "上海", "2026-07-01")).thenReturn(mockResponse);

        Map<String, Object> result = flightSearchController.searchFlights("北京", "上海", "2026-07-01");

        assertTrue((Boolean) result.get("success"));
        assertEquals(mockResponse, result.get("data"));
        verify(mcpProxyService).searchFlights("北京", "上海", "2026-07-01");
    }

    @Test
    void searchFlights_whenException_shouldReturnSuccessFalse() {
        when(mcpProxyService.searchFlights(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("API调用失败"));

        Map<String, Object> result = flightSearchController.searchFlights("北京", "上海", "2026-07-01");

        assertFalse((Boolean) result.get("success"));
        assertEquals("API调用失败", result.get("error"));
    }
}
