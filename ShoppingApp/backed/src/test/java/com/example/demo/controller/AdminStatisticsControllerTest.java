package com.example.demo.controller;

import com.example.demo.service.AdminStatisticsService;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelOrderService;
import com.example.ordermanagement.service.HotelService;
import com.example.ordermanagement.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsControllerTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private HotelOrderService orderService;

    @Mock
    private MessageService messageService;

    @Mock
    private AdminStatisticsService adminStatisticsService;

    @InjectMocks
    private AdminStatisticsController adminStatisticsController;

    @Test
    void getStatistics_shouldReturnAllCounts() {
        when(adminStatisticsService.countAllHotels()).thenReturn(10L);
        when(adminStatisticsService.countHotelsByStatus("pending")).thenReturn(2L);
        when(adminStatisticsService.countHotelsByStatus("approved")).thenReturn(7L);
        when(adminStatisticsService.countHotelsByStatus("violation")).thenReturn(1L);

        when(adminStatisticsService.countAllMerchants()).thenReturn(5L);
        when(adminStatisticsService.countMerchantsByStatus("pending")).thenReturn(1L);
        when(adminStatisticsService.countMerchantsByStatus("approved")).thenReturn(3L);
        when(adminStatisticsService.countMerchantsByStatus("banned")).thenReturn(1L);

        when(adminStatisticsService.countAllUsers()).thenReturn(100L);
        when(adminStatisticsService.countUsersByStatus("active")).thenReturn(90L);
        when(adminStatisticsService.countUsersByStatus("banned")).thenReturn(10L);

        when(orderService.countAll()).thenReturn(50L);
        when(orderService.countByStatus("待支付")).thenReturn(10L);
        when(orderService.countByStatus("待确认")).thenReturn(5L);
        when(orderService.countByStatus("已入住")).thenReturn(20L);
        when(orderService.countByStatus("已完成")).thenReturn(10L);
        when(orderService.countByStatus("已取消")).thenReturn(5L);

        when(adminStatisticsService.countAllComments()).thenReturn(200L);
        when(adminStatisticsService.countViolationComments()).thenReturn(3L);
        when(adminStatisticsService.countPendingAppeals()).thenReturn(5L);

        when(adminStatisticsService.countTodayNewHotels()).thenReturn(1L);
        when(adminStatisticsService.countTodayNewOrders()).thenReturn(3L);
        when(adminStatisticsService.countTodayNewUsers()).thenReturn(5L);
        when(adminStatisticsService.getTodayRevenue()).thenReturn(BigDecimal.valueOf(5000));

        Result<Map<String, Object>> result = adminStatisticsController.getStatistics();

        assertEquals(200, result.getCode());
        Map<String, Object> stats = result.getData();
        assertEquals(10L, stats.get("totalHotels"));
        assertEquals(100L, stats.get("totalUsers"));
        assertEquals(50L, stats.get("totalOrders"));
        assertEquals(200L, stats.get("totalComments"));
        assertEquals(5L, stats.get("pendingAppeals"));
        assertEquals(BigDecimal.valueOf(5000), stats.get("todayRevenue"));
    }

    @Test
    void getStatistics_whenException_shouldReturnError() {
        when(adminStatisticsService.countAllHotels()).thenThrow(new RuntimeException("DB错误"));

        Result<Map<String, Object>> result = adminStatisticsController.getStatistics();

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("获取统计数据失败"));
    }

    @Test
    void getBusinessAnalysis_shouldReturnAllData() {
        when(adminStatisticsService.getRevenueTrend(anyString(), any(), any())).thenReturn(new ArrayList<>());
        when(adminStatisticsService.getOrderTrend(anyString(), any(), any())).thenReturn(new ArrayList<>());
        when(adminStatisticsService.getHotelRevenueRank()).thenReturn(new ArrayList<>());
        when(adminStatisticsService.getOrderStatusDistribution()).thenReturn(new LinkedHashMap<>());
        when(adminStatisticsService.getMonthlyMetrics(any(), any())).thenReturn(new HashMap<>());
        when(adminStatisticsService.getPopularHotels(10)).thenReturn(new ArrayList<>());
        when(adminStatisticsService.getAverageOccupancy(anyString())).thenReturn(BigDecimal.valueOf(75.5));
        when(adminStatisticsService.getTotalRevenue(anyString())).thenReturn(BigDecimal.valueOf(100000));
        when(adminStatisticsService.getTotalOrders(anyString())).thenReturn(200);
        when(adminStatisticsService.getAverageOrderPrice(anyString())).thenReturn(BigDecimal.valueOf(500));
        when(adminStatisticsService.getHotelCommentStats()).thenReturn(new ArrayList<>());

        Result<Map<String, Object>> result = adminStatisticsController.getBusinessAnalysis("6months", 2026, 6);

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("revenueTrend"));
        assertNotNull(data.get("orderTrend"));
        assertNotNull(data.get("hotelRevenueRank"));
        assertNotNull(data.get("orderStatusDistribution"));
        assertNotNull(data.get("monthlyMetrics"));
        assertNotNull(data.get("popularHotels"));
        assertEquals(BigDecimal.valueOf(75.5), data.get("averageOccupancy"));
        assertEquals(BigDecimal.valueOf(100000), data.get("totalRevenue"));
        assertEquals(200, data.get("totalOrders"));
        assertEquals(BigDecimal.valueOf(500), data.get("avgPrice"));
    }

    @Test
    void getBusinessAnalysis_whenException_shouldReturnError() {
        when(adminStatisticsService.getRevenueTrend(anyString(), any(), any()))
            .thenThrow(new RuntimeException("分析失败"));

        Result<Map<String, Object>> result = adminStatisticsController.getBusinessAnalysis(null, null, null);

        assertEquals(500, result.getCode());
    }

    @Test
    void getAnalysisChartData_revenue_shouldReturnMonthlyRevenue() {
        List<BigDecimal> monthlyRevenue = Arrays.asList(
            BigDecimal.valueOf(10000), BigDecimal.valueOf(12000)
        );
        when(adminStatisticsService.getMonthlyRevenue(2026)).thenReturn(monthlyRevenue);

        Result<Map<String, Object>> result = adminStatisticsController.getAnalysisChartData("revenue", 2026);

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("months"));
        assertNotNull(data.get("values"));
    }

    @Test
    void getAnalysisChartData_orders_shouldReturnMonthlyOrders() {
        List<Long> monthlyOrders = Arrays.asList(10L, 20L);
        when(adminStatisticsService.getMonthlyOrders(2026)).thenReturn(monthlyOrders);

        Result<Map<String, Object>> result = adminStatisticsController.getAnalysisChartData("orders", 2026);

        assertEquals(200, result.getCode());
    }

    @Test
    void getAnalysisChartData_whenException_shouldReturnError() {
        when(adminStatisticsService.getMonthlyRevenue(2026))
            .thenThrow(new RuntimeException("查询失败"));

        Result<Map<String, Object>> result = adminStatisticsController.getAnalysisChartData("revenue", 2026);

        assertEquals(500, result.getCode());
    }
}
