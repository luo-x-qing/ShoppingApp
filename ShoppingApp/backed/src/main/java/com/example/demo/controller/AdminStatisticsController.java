package com.example.demo.controller;

import com.example.ordermanagement.model.Result;
import com.example.demo.service.AdminStatisticsService;
import com.example.ordermanagement.service.HotelOrderService;
import com.example.ordermanagement.service.HotelService;
import com.example.ordermanagement.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 管理员统计分析控制器
 * 提供数据统计和经营分析功能
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminStatisticsController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private HotelOrderService orderService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 获取管理员仪表盘统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 🔥 酒店统计 - 直接使用 AdminStatisticsService 的方法
            stats.put("totalHotels", adminStatisticsService.countAllHotels());
            stats.put("pendingHotels", adminStatisticsService.countHotelsByStatus("pending"));
            stats.put("approvedHotels", adminStatisticsService.countHotelsByStatus("approved"));
            stats.put("violationHotels", adminStatisticsService.countHotelsByStatus("violation"));
            
            // 商家统计
            stats.put("totalMerchants", adminStatisticsService.countAllMerchants());
            stats.put("pendingMerchants", adminStatisticsService.countMerchantsByStatus("pending"));
            stats.put("approvedMerchants", adminStatisticsService.countMerchantsByStatus("approved"));
            stats.put("bannedMerchants", adminStatisticsService.countMerchantsByStatus("banned"));
            
            // 用户统计
            stats.put("totalUsers", adminStatisticsService.countAllUsers());
            stats.put("activeUsers", adminStatisticsService.countUsersByStatus("active"));
            stats.put("bannedUsers", adminStatisticsService.countUsersByStatus("banned"));
            
            // 订单统计 - 使用 HotelOrderService
            stats.put("totalOrders", orderService.countAll());
            stats.put("pendingOrders", orderService.countByStatus("待支付"));
            stats.put("waitConfirmOrders", orderService.countByStatus("待确认"));
            stats.put("checkinOrders", orderService.countByStatus("已入住"));
            stats.put("completedOrders", orderService.countByStatus("已完成"));
            stats.put("cancelledOrders", orderService.countByStatus("已取消"));
            
            // 评价统计
            stats.put("totalComments", adminStatisticsService.countAllComments());
            stats.put("violationComments", adminStatisticsService.countViolationComments());
            
            // 申诉统计
            stats.put("pendingAppeals", adminStatisticsService.countPendingAppeals());
            
            // 今日新增
            stats.put("todayNewHotels", adminStatisticsService.countTodayNewHotels());
            stats.put("todayNewOrders", adminStatisticsService.countTodayNewOrders());
            stats.put("todayNewUsers", adminStatisticsService.countTodayNewUsers());
            stats.put("todayRevenue", adminStatisticsService.getTodayRevenue());
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取酒店经营分析数据
     */
    @GetMapping("/business-analysis")
    public Result<Map<String, Object>> getBusinessAnalysis(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            if (period == null) period = "6months";
            
            // 1. 收入趋势
            List<Map<String, Object>> revenueTrend = adminStatisticsService.getRevenueTrend(period, year, month);
            analysis.put("revenueTrend", revenueTrend);
            
            // 2. 订单趋势
            List<Map<String, Object>> orderTrend = adminStatisticsService.getOrderTrend(period, year, month);
            analysis.put("orderTrend", orderTrend);
            
            // 3. 各酒店收入排名
            List<Map<String, Object>> hotelRevenueRank = adminStatisticsService.getHotelRevenueRank();
            analysis.put("hotelRevenueRank", hotelRevenueRank);
            
            // 4. 订单状态分布
            Map<String, Integer> orderStatusDistribution = adminStatisticsService.getOrderStatusDistribution();
            analysis.put("orderStatusDistribution", orderStatusDistribution);
            
            // 5. 月度指标
            Map<String, Object> monthlyMetrics = adminStatisticsService.getMonthlyMetrics(year, month);
            analysis.put("monthlyMetrics", monthlyMetrics);
            
            // 6. 热门酒店
            List<Map<String, Object>> popularHotels = adminStatisticsService.getPopularHotels(10);
            analysis.put("popularHotels", popularHotels);
            
            // 7. 平均入住率
            BigDecimal averageOccupancy = adminStatisticsService.getAverageOccupancy(period);
            analysis.put("averageOccupancy", averageOccupancy);
            
            // 8. 总收入
            BigDecimal totalRevenue = adminStatisticsService.getTotalRevenue(period);
            analysis.put("totalRevenue", totalRevenue);
            
            // 9. 总订单数
            Integer totalOrders = adminStatisticsService.getTotalOrders(period);
            analysis.put("totalOrders", totalOrders);
            
            // 10. 平均客单价
            BigDecimal avgPrice = adminStatisticsService.getAverageOrderPrice(period);
            analysis.put("avgPrice", avgPrice);
            
            // 11. 各酒店评价统计
            List<Map<String, Object>> hotelCommentStats = adminStatisticsService.getHotelCommentStats();
            analysis.put("hotelCommentStats", hotelCommentStats);
            
            return Result.success(analysis);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取经营分析数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取经营分析图表数据
     */
    @GetMapping("/analysis-chart-data")
    public Result<Map<String, Object>> getAnalysisChartData(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer year) {
        try {
            Map<String, Object> chartData = new HashMap<>();
            
            if (year == null) year = LocalDate.now().getYear();
            
            switch (type) {
                case "revenue":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", adminStatisticsService.getMonthlyRevenue(year));
                    break;
                case "orders":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", adminStatisticsService.getMonthlyOrders(year));
                    break;
                case "occupancy":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", adminStatisticsService.getMonthlyOccupancy(year));
                    break;
                case "hotelRevenue":
                    List<Map<String, Object>> hotels = adminStatisticsService.getHotelRevenueRank();
                    chartData.put("names", hotels.stream().map(m -> m.get("hotelName").toString()).toArray());
                    chartData.put("values", hotels.stream().map(m -> m.get("revenue")).toArray());
                    break;
                default:
                    chartData.put("months", getMonthNames());
                    chartData.put("values", adminStatisticsService.getMonthlyRevenue(year));
            }
            
            return Result.success(chartData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取图表数据失败：" + e.getMessage());
        }
    }

    private List<String> getMonthNames() {
        return Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月");
    }
}