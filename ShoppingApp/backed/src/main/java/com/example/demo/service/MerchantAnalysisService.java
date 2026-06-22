package com.example.demo.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.repository.HotelCommentRepository;
import com.example.ordermanagement.repository.HotelOrderRepository;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantAnalysisService {

    @Autowired
    private HotelOrderRepository orderRepository;

    @Autowired
    private HotelCommentRepository commentRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    /**
     * 获取收入趋势
     */
    public List<Map<String, Object>> getRevenueTrend(List<Long> hotelIds, String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int months = getMonths(period);

        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            LocalDateTime start = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            // 🔥 修复：使用 BigDecimal 累加
            BigDecimal revenue = BigDecimal.ZERO;
            for (HotelOrder order : allOrders) {
                if (order.getCreateTime() == null) continue;
                if (!order.getCreateTime().isAfter(start) || !order.getCreateTime().isBefore(end)) continue;
                if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;

                Double price = order.getPrice();
                if (price != null) {
                    revenue = revenue.add(BigDecimal.valueOf(price));
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthKey);
            item.put("revenue", revenue);
            result.add(item);
        }

        return result;
    }

    /**
     * 获取订单趋势
     */
    public List<Map<String, Object>> getOrderTrend(List<Long> hotelIds, String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int months = getMonths(period);

        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

            LocalDateTime start = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long count = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                    .count();

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthKey);
            item.put("orders", count);
            result.add(item);
        }

        return result;
    }

    /**
     * 各酒店收入排名
     */
    public List<Map<String, Object>> getHotelRevenueRank(List<Long> hotelIds) {
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        Map<Long, Map<String, Object>> hotelRevenueMap = new HashMap<>();

        for (HotelOrder order : allOrders) {
            if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;

            Long hotelId = order.getHotelId();
            if (!hotelRevenueMap.containsKey(hotelId)) {
                Map<String, Object> hotelData = new HashMap<>();
                hotelData.put("hotelId", hotelId);
                hotelData.put("hotelName", order.getName() != null ? order.getName() : "酒店" + hotelId);
                hotelData.put("revenue", BigDecimal.ZERO);
                hotelData.put("orderCount", 0L);
                hotelRevenueMap.put(hotelId, hotelData);
            }

            Map<String, Object> hotelData = hotelRevenueMap.get(hotelId);
            BigDecimal currentRevenue = (BigDecimal) hotelData.get("revenue");
            Double price = order.getPrice();
            if (price != null) {
                hotelData.put("revenue", currentRevenue.add(BigDecimal.valueOf(price)));
            }
            hotelData.put("orderCount", ((Long) hotelData.get("orderCount")) + 1);
        }

        List<Map<String, Object>> result = new ArrayList<>(hotelRevenueMap.values());
        result.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));

        return result;
    }

    /**
     * 订单状态分布
     */
    public Map<String, Integer> getOrderStatusDistribution(List<Long> hotelIds) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        Map<String, Long> statusCount = allOrders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(HotelOrder::getStatus, Collectors.counting()));

        distribution.put("待支付", statusCount.getOrDefault("待支付", 0L).intValue());
        distribution.put("待确认", statusCount.getOrDefault("待确认", 0L).intValue());
        distribution.put("已支付", statusCount.getOrDefault("已支付", 0L).intValue());
        distribution.put("已确认", statusCount.getOrDefault("已确认", 0L).intValue());
        distribution.put("已入住", statusCount.getOrDefault("已入住", 0L).intValue());
        distribution.put("已完成", statusCount.getOrDefault("已完成", 0L).intValue());
        distribution.put("已取消", statusCount.getOrDefault("已取消", 0L).intValue());
        distribution.put("取消申请中", statusCount.getOrDefault("取消申请中", 0L).intValue());

        return distribution;
    }

    /**
     * 月度指标
     */
    public Map<String, Object> getMonthlyMetrics(List<Long> hotelIds, String period) {
        Map<String, Object> metrics = new HashMap<>();
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        // 🔥 修复：使用 for 循环累加 BigDecimal
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (HotelOrder order : allOrders) {
            if ("已完成".equals(order.getStatus()) || "已入住".equals(order.getStatus())) {
                Double price = order.getPrice();
                if (price != null) {
                    totalRevenue = totalRevenue.add(BigDecimal.valueOf(price));
                }
            }
        }
        metrics.put("totalRevenue", totalRevenue);

        // 总订单数
        metrics.put("totalOrders", allOrders.size());

        // 已完成订单数
        long completedOrders = allOrders.stream()
                .filter(o -> "已完成".equals(o.getStatus()))
                .count();
        metrics.put("completedOrders", completedOrders);

        // 待处理订单
        long pendingOrders = allOrders.stream()
                .filter(o -> "待支付".equals(o.getStatus()) || "待确认".equals(o.getStatus()))
                .count();
        metrics.put("pendingOrders", pendingOrders);

        // 平均客单价
        BigDecimal avgPrice = BigDecimal.ZERO;
        List<HotelOrder> completedOrdersList = allOrders.stream()
                .filter(o -> "已完成".equals(o.getStatus()) || "已入住".equals(o.getStatus()))
                .collect(Collectors.toList());
        if (!completedOrdersList.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (HotelOrder order : completedOrdersList) {
                Double price = order.getPrice();
                if (price != null) {
                    total = total.add(BigDecimal.valueOf(price));
                }
            }
            avgPrice = total.divide(BigDecimal.valueOf(completedOrdersList.size()), 2, RoundingMode.HALF_UP);
        }
        metrics.put("avgPrice", avgPrice);

        return metrics;
    }

    /**
     * 总收入
     */
    public BigDecimal getTotalRevenue(List<Long> hotelIds, String period) {
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);
        int months = getMonths(period);
        LocalDate now = LocalDate.now();

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            LocalDateTime start = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            BigDecimal monthRevenue = BigDecimal.ZERO;
            for (HotelOrder order : allOrders) {
                if (order.getCreateTime() == null) continue;
                if (!order.getCreateTime().isAfter(start) || !order.getCreateTime().isBefore(end)) continue;
                if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;

                Double price = order.getPrice();
                if (price != null) {
                    monthRevenue = monthRevenue.add(BigDecimal.valueOf(price));
                }
            }
            total = total.add(monthRevenue);
        }

        return total;
    }

    /**
     * 总订单数
     */
    public Integer getTotalOrders(List<Long> hotelIds, String period) {
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);
        int months = getMonths(period);
        LocalDate now = LocalDate.now();

        long total = 0;
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            LocalDateTime start = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long count = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                    .count();
            total += count;
        }

        return (int) total;
    }

    /**
     * 平均客单价
     */
    public BigDecimal getAverageOrderPrice(List<Long> hotelIds, String period) {
        BigDecimal totalRevenue = getTotalRevenue(hotelIds, period);
        Integer totalOrders = getTotalOrders(hotelIds, period);

        if (totalOrders == 0) return BigDecimal.ZERO;
        return totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
    }

    /**
     * 平均入住率
     */
    public BigDecimal getAverageOccupancy(List<Long> hotelIds, String period) {
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);
        int months = getMonths(period);
        LocalDate now = LocalDate.now();

        long totalRooms = 0;
        for (Long hotelId : hotelIds) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null && hotel.getTotalRooms() != null) {
                totalRooms += hotel.getTotalRooms();
            }
        }
        if (totalRooms == 0) {
            totalRooms = roomTypeRepository.count();
        }
        if (totalRooms == 0) return BigDecimal.ZERO;

        BigDecimal totalOccupancy = BigDecimal.ZERO;
        int count = 0;

        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            LocalDateTime start = LocalDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long occupiedRooms = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                    .filter(o -> "已入住".equals(o.getStatus()) || "已完成".equals(o.getStatus()))
                    .count();

            BigDecimal occupancy = BigDecimal.valueOf(occupiedRooms)
                    .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            totalOccupancy = totalOccupancy.add(occupancy);
            count++;
        }

        if (count == 0) return BigDecimal.ZERO;
        return totalOccupancy.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * 各酒店评价统计
     */
    public List<Map<String, Object>> getHotelCommentStats(List<Long> hotelIds) {
        List<HotelComment> allComments = commentRepository.findByHotelIds(hotelIds);

        Map<Long, String> hotelNameMap = new HashMap<>();
        for (Long hotelId : hotelIds) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null) {
                hotelNameMap.put(hotelId, hotel.getName());
            }
        }

        Map<Long, Map<String, Object>> hotelCommentMap = new HashMap<>();

        for (HotelComment comment : allComments) {
            Long hotelId = comment.getHotelId();
            if (!hotelCommentMap.containsKey(hotelId)) {
                Map<String, Object> hotelData = new HashMap<>();
                hotelData.put("hotelId", hotelId);
                hotelData.put("hotelName", hotelNameMap.getOrDefault(hotelId, "酒店" + hotelId));
                hotelData.put("commentCount", 0L);
                hotelData.put("totalScore", 0L);
                hotelCommentMap.put(hotelId, hotelData);
            }

            Map<String, Object> hotelData = hotelCommentMap.get(hotelId);
            hotelData.put("commentCount", ((Long) hotelData.get("commentCount")) + 1);
            Integer score = comment.getScore();
            Long totalScore = (Long) hotelData.get("totalScore");
            hotelData.put("totalScore", totalScore + (score != null ? score.longValue() : 0L));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> hotelData : hotelCommentMap.values()) {
            Long commentCount = (Long) hotelData.get("commentCount");
            Long totalScore = (Long) hotelData.get("totalScore");
            hotelData.put("avgScore", commentCount > 0 ?
                    BigDecimal.valueOf(totalScore).divide(BigDecimal.valueOf(commentCount), 1, RoundingMode.HALF_UP).doubleValue() : 0.0);
            result.add(hotelData);
        }

        result.sort((a, b) -> ((Double) b.get("avgScore")).compareTo((Double) a.get("avgScore")));

        return result;
    }

    /**
     * 月度收入（图表）
     */
    public List<BigDecimal> getMonthlyRevenue(List<Long> hotelIds, int year) {
        List<BigDecimal> revenues = new ArrayList<>();
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        for (int month = 1; month <= 12; month++) {
            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            BigDecimal revenue = BigDecimal.ZERO;
            for (HotelOrder order : allOrders) {
                if (order.getCreateTime() == null) continue;
                if (!order.getCreateTime().isAfter(start) || !order.getCreateTime().isBefore(end)) continue;
                if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;

                Double price = order.getPrice();
                if (price != null) {
                    revenue = revenue.add(BigDecimal.valueOf(price));
                }
            }

            revenues.add(revenue);
        }

        return revenues;
    }

    /**
     * 月度订单（图表）
     */
    public List<Long> getMonthlyOrders(List<Long> hotelIds, int year) {
        List<Long> orders = new ArrayList<>();
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        for (int month = 1; month <= 12; month++) {
            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long count = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                    .count();

            orders.add(count);
        }

        return orders;
    }

    /**
     * 月度入住率（图表）
     */
    public List<BigDecimal> getMonthlyOccupancy(List<Long> hotelIds, int year) {
        List<BigDecimal> occupancies = new ArrayList<>();
        List<HotelOrder> allOrders = orderRepository.findByHotelIds(hotelIds);

        long totalRooms = 0;
        for (Long hotelId : hotelIds) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null && hotel.getTotalRooms() != null) {
                totalRooms += hotel.getTotalRooms();
            }
        }
        if (totalRooms == 0) {
            totalRooms = roomTypeRepository.count();
        }

        for (int month = 1; month <= 12; month++) {
            if (totalRooms == 0) {
                occupancies.add(BigDecimal.ZERO);
                continue;
            }

            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);

            long occupiedRooms = allOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                    .filter(o -> "已入住".equals(o.getStatus()) || "已完成".equals(o.getStatus()))
                    .count();

            BigDecimal occupancy = BigDecimal.valueOf(occupiedRooms)
                    .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            occupancies.add(occupancy.setScale(2, RoundingMode.HALF_UP));
        }

        return occupancies;
    }

    private int getMonths(String period) {
        if ("12months".equals(period)) return 12;
        if ("3months".equals(period)) return 3;
        return 6;
    }
}