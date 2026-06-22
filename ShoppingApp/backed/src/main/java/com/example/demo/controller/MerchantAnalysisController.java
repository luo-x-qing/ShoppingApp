package com.example.demo.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelService;
import com.example.demo.service.MerchantAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantAnalysisController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private MerchantAnalysisService merchantAnalysisService;

    /**
     * 获取商家经营分析数据
     */
    @GetMapping("/business-analysis")
    public Result<Map<String, Object>> getBusinessAnalysis(
            @RequestParam Long merchantId,
            @RequestParam(required = false) String period) {
        try {
            if (merchantId == null) {
                return Result.error("商家ID不能为空");
            }
            
            if (period == null) period = "6months";
            
            // 获取该商家的所有酒店（数据隔离）
            List<Hotel> hotels = hotelService.getHotelsByMerchant(merchantId);
            if (hotels.isEmpty()) {
                return Result.error("该商家暂无酒店");
            }
            
            List<Long> hotelIds = hotels.stream()
                    .map(Hotel::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> analysis = new HashMap<>();
            
            // 1. 商家基本信息
            Map<String, Object> merchantInfo = new HashMap<>();
            merchantInfo.put("merchantId", merchantId);
            merchantInfo.put("hotelCount", hotels.size());
            merchantInfo.put("hotelNames", hotels.stream()
                    .map(Hotel::getName)
                    .collect(java.util.stream.Collectors.toList()));
            analysis.put("merchantInfo", merchantInfo);
            
            // 2. 收入趋势
            List<Map<String, Object>> revenueTrend = merchantAnalysisService.getRevenueTrend(hotelIds, period);
            analysis.put("revenueTrend", revenueTrend);
            
            // 3. 订单趋势
            List<Map<String, Object>> orderTrend = merchantAnalysisService.getOrderTrend(hotelIds, period);
            analysis.put("orderTrend", orderTrend);
            
            // 4. 各酒店收入排名
            List<Map<String, Object>> hotelRevenueRank = merchantAnalysisService.getHotelRevenueRank(hotelIds);
            analysis.put("hotelRevenueRank", hotelRevenueRank);
            
            // 5. 订单状态分布
            Map<String, Integer> orderStatusDistribution = merchantAnalysisService.getOrderStatusDistribution(hotelIds);
            analysis.put("orderStatusDistribution", orderStatusDistribution);
            
            // 6. 月度指标
            Map<String, Object> monthlyMetrics = merchantAnalysisService.getMonthlyMetrics(hotelIds, period);
            analysis.put("monthlyMetrics", monthlyMetrics);
            
            // 7. 总收入
            BigDecimal totalRevenue = merchantAnalysisService.getTotalRevenue(hotelIds, period);
            analysis.put("totalRevenue", totalRevenue);
            
            // 8. 总订单数
            Integer totalOrders = merchantAnalysisService.getTotalOrders(hotelIds, period);
            analysis.put("totalOrders", totalOrders);
            
            // 9. 平均客单价
            BigDecimal avgPrice = merchantAnalysisService.getAverageOrderPrice(hotelIds, period);
            analysis.put("avgPrice", avgPrice);
            
            // 10. 平均入住率
            BigDecimal averageOccupancy = merchantAnalysisService.getAverageOccupancy(hotelIds, period);
            analysis.put("averageOccupancy", averageOccupancy);
            
            // 11. 各酒店评价统计
            List<Map<String, Object>> hotelCommentStats = merchantAnalysisService.getHotelCommentStats(hotelIds);
            analysis.put("hotelCommentStats", hotelCommentStats);
            
            return Result.success(analysis);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取经营分析数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取商家经营分析图表数据
     */
    @GetMapping("/analysis-chart-data")
    public Result<Map<String, Object>> getAnalysisChartData(
            @RequestParam Long merchantId,
            @RequestParam String type,
            @RequestParam(required = false) Integer year) {
        try {
            if (merchantId == null) {
                return Result.error("商家ID不能为空");
            }
            
            if (year == null) year = LocalDate.now().getYear();
            
            List<Hotel> hotels = hotelService.getHotelsByMerchant(merchantId);
            if (hotels.isEmpty()) {
                return Result.error("该商家暂无酒店");
            }
            
            List<Long> hotelIds = hotels.stream()
                    .map(Hotel::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> chartData = new HashMap<>();
            
            switch (type) {
                case "revenue":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", merchantAnalysisService.getMonthlyRevenue(hotelIds, year));
                    break;
                case "orders":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", merchantAnalysisService.getMonthlyOrders(hotelIds, year));
                    break;
                case "occupancy":
                    chartData.put("months", getMonthNames());
                    chartData.put("values", merchantAnalysisService.getMonthlyOccupancy(hotelIds, year));
                    break;
                default:
                    chartData.put("months", getMonthNames());
                    chartData.put("values", merchantAnalysisService.getMonthlyRevenue(hotelIds, year));
            }
            
            return Result.success(chartData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取图表数据失败：" + e.getMessage());
        }
    }

    private List<String> getMonthNames() {
        return Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月", 
                            "7月", "8月", "9月", "10月", "11月", "12月");
    }
}