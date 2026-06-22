package com.example.demo.service;

import com.example.demo.model.Appeal;
import com.example.demo.model.User;
import com.example.demo.repository.AppealRepository;
import com.example.demo.repository.UserRepository;
import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStatisticsService {

    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private HotelOrderRepository orderRepository;
    
    @Autowired
    private HotelCommentRepository commentRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppealRepository appealRepository;

    // ========== 🔥 酒店统计（直接从 Repository 查询，不依赖 HotelService） ==========
    
    /**
     * 统计所有酒店数量
     */
    public long countAllHotels() {
        return hotelRepository.count();
    }
    
    /**
     * 按状态统计酒店数量（支持中文状态值）
     */
    public long countHotelsByStatus(String status) {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .filter(h -> h.getStatus() != null)
                .filter(h -> {
                    String hotelStatus = h.getStatus();
                    // 根据传入的状态参数进行匹配
                    if ("pending".equalsIgnoreCase(status)) {
                        return "pending".equalsIgnoreCase(hotelStatus) || 
                               "PENDING".equalsIgnoreCase(hotelStatus);
                    } else if ("approved".equalsIgnoreCase(status)) {
                        return "approved".equalsIgnoreCase(hotelStatus) || 
                               "APPROVED".equalsIgnoreCase(hotelStatus) || 
                               "营业中".equals(hotelStatus) ||
                               "NORMAL".equalsIgnoreCase(hotelStatus);
                    } else if ("violation".equalsIgnoreCase(status)) {
                        return "violation".equalsIgnoreCase(hotelStatus) || 
                               "VIOLATION".equalsIgnoreCase(hotelStatus) || 
                               "违规".equals(hotelStatus) || 
                               "已停业".equals(hotelStatus) ||
                               "SUSPENDED".equalsIgnoreCase(hotelStatus);
                    }
                    return hotelStatus.equals(status);
                })
                .count();
    }
    
    public long countTodayNewHotels() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .filter(h -> h.getUpdateTime() != null)
                .filter(h -> h.getUpdateTime().isAfter(startOfDay) && h.getUpdateTime().isBefore(endOfDay))
                .count();
    }
    
    public long countHotelsByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .filter(h -> h.getUpdateTime() != null)
                .filter(h -> h.getUpdateTime().isAfter(start) && h.getUpdateTime().isBefore(end))
                .count();
    }

    // ========== 商家统计 ==========
    
    public long countAllMerchants() {
        return userRepository.countByRole("MERCHANT");
    }
    
    public long countMerchantsByStatus(String status) {
        return userRepository.countByRoleAndStatus("MERCHANT", status);
    }

    // ========== 用户统计 ==========
    
    public long countAllUsers() {
        return userRepository.countByRole("USER");
    }
    
    public long countUsersByStatus(String status) {
        return userRepository.countByRoleAndStatus("USER", status);
    }
    
    public long countTodayNewUsers() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<User> users = userRepository.findByRole("USER");
        return users.stream()
                .filter(u -> u.getCreateTime() != null)
                .filter(u -> u.getCreateTime().isAfter(startOfDay) && u.getCreateTime().isBefore(endOfDay))
                .count();
    }
    
    public long countUsersByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<User> users = userRepository.findByRole("USER");
        return users.stream()
                .filter(u -> u.getCreateTime() != null)
                .filter(u -> u.getCreateTime().isAfter(start) && u.getCreateTime().isBefore(end))
                .count();
    }

    // ========== 订单统计 ==========
    
    public long countAllOrders() {
        return orderRepository.count();
    }
    
    public long countOrdersByStatus(String status) {
        List<HotelOrder> orders = orderRepository.findByStatus(status);
        return orders.size();
    }
    
    public long countTodayNewOrders() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<HotelOrder> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .filter(o -> o.getCreateTime() != null)
                .filter(o -> o.getCreateTime().isAfter(startOfDay) && o.getCreateTime().isBefore(endOfDay))
                .count();
    }
    
    public long countOrdersByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<HotelOrder> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .filter(o -> o.getCreateTime() != null)
                .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                .count();
    }
    
    public BigDecimal getRevenueByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<HotelOrder> allOrders = orderRepository.findAll();
        
        BigDecimal total = BigDecimal.ZERO;
        for (HotelOrder order : allOrders) {
            if (order.getCreateTime() == null) continue;
            if (!order.getCreateTime().isAfter(start) || !order.getCreateTime().isBefore(end)) continue;
            if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;
            
            Double price = order.getPrice();
            if (price != null) {
                total = total.add(BigDecimal.valueOf(price));
            }
        }
        return total;
    }
    
    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<HotelOrder> allOrders = orderRepository.findAll();
        
        BigDecimal total = BigDecimal.ZERO;
        for (HotelOrder order : allOrders) {
            if (order.getCreateTime() == null) continue;
            if (!order.getCreateTime().isAfter(startOfDay) || !order.getCreateTime().isBefore(endOfDay)) continue;
            if (!"已完成".equals(order.getStatus()) && !"已入住".equals(order.getStatus())) continue;
            
            Double price = order.getPrice();
            if (price != null) {
                total = total.add(BigDecimal.valueOf(price));
            }
        }
        return total;
    }

    // ========== 评价统计 ==========
    
    public long countAllComments() {
        return commentRepository.count();
    }
    
    public long countViolationComments() {
        List<HotelComment> comments = commentRepository.findByStatus("违规");
        if (comments.isEmpty()) {
            comments = commentRepository.findByStatus("VIOLATION");
        }
        return comments.size();
    }

    // ========== 申诉统计 ==========
    
    public long countPendingAppeals() {
        return appealRepository.countPending();
    }

    // ========== 经营分析方法 ==========
    
    public List<Map<String, Object>> getRevenueTrend(String period, Integer year, Integer month) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        int months = getMonths(period);
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            BigDecimal revenue = getRevenueByMonth(date.getYear(), date.getMonthValue());
            
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthKey);
            item.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
            result.add(item);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getOrderTrend(String period, Integer year, Integer month) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        int months = getMonths(period);
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            Long orderCount = countOrdersByMonth(date.getYear(), date.getMonthValue());
            
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthKey);
            item.put("orders", orderCount != null ? orderCount : 0L);
            result.add(item);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getHotelRevenueRank() {
        List<HotelOrder> allOrders = orderRepository.findAll();
        
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
    
    public Map<String, Integer> getOrderStatusDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        List<HotelOrder> allOrders = orderRepository.findAll();
        
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
    
    public Map<String, Object> getMonthlyMetrics(Integer year, Integer month) {
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();
        
        Map<String, Object> metrics = new HashMap<>();
        
        BigDecimal revenue = getRevenueByMonth(year, month);
        metrics.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        
        Long orders = countOrdersByMonth(year, month);
        metrics.put("orders", orders != null ? orders : 0L);
        
        BigDecimal occupancy = calculateMonthlyOccupancy(year, month);
        metrics.put("occupancy", occupancy != null ? occupancy : BigDecimal.ZERO);
        
        BigDecimal avgPrice = getAveragePriceByMonth(year, month);
        metrics.put("avgPrice", avgPrice != null ? avgPrice : BigDecimal.ZERO);
        
        Long newUsers = countUsersByMonth(year, month);
        metrics.put("newUsers", newUsers != null ? newUsers : 0L);
        
        Long newHotels = countHotelsByMonth(year, month);
        metrics.put("newHotels", newHotels != null ? newHotels : 0L);
        
        return metrics;
    }
    
    public List<Map<String, Object>> getPopularHotels(int limit) {
        List<HotelOrder> allOrders = orderRepository.findAll();
        
        Map<Long, Map<String, Object>> hotelOrderMap = new HashMap<>();
        
        for (HotelOrder order : allOrders) {
            Long hotelId = order.getHotelId();
            if (!hotelOrderMap.containsKey(hotelId)) {
                Map<String, Object> hotelData = new HashMap<>();
                hotelData.put("hotelId", hotelId);
                hotelData.put("hotelName", order.getName() != null ? order.getName() : "酒店" + hotelId);
                hotelData.put("orderCount", 0L);
                hotelOrderMap.put(hotelId, hotelData);
            }
            
            Map<String, Object> hotelData = hotelOrderMap.get(hotelId);
            hotelData.put("orderCount", ((Long) hotelData.get("orderCount")) + 1);
        }
        
        List<Map<String, Object>> result = new ArrayList<>(hotelOrderMap.values());
        result.sort((a, b) -> ((Long) b.get("orderCount")).compareTo((Long) a.get("orderCount")));
        
        return result.stream().limit(limit).collect(Collectors.toList());
    }
    
    public BigDecimal getAverageOccupancy(String period) {
        int months = getMonths(period);
        BigDecimal totalOccupancy = BigDecimal.ZERO;
        int count = 0;
        
        LocalDate now = LocalDate.now();
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            BigDecimal occupancy = calculateMonthlyOccupancy(date.getYear(), date.getMonthValue());
            if (occupancy != null) {
                totalOccupancy = totalOccupancy.add(occupancy);
                count++;
            }
        }
        
        if (count == 0) return BigDecimal.ZERO;
        return totalOccupancy.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getTotalRevenue(String period) {
        int months = getMonths(period);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        LocalDate now = LocalDate.now();
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            BigDecimal revenue = getRevenueByMonth(date.getYear(), date.getMonthValue());
            if (revenue != null) {
                totalRevenue = totalRevenue.add(revenue);
            }
        }
        
        return totalRevenue;
    }
    
    public Integer getTotalOrders(String period) {
        int months = getMonths(period);
        Long totalOrders = 0L;
        
        LocalDate now = LocalDate.now();
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i);
            Long orders = countOrdersByMonth(date.getYear(), date.getMonthValue());
            if (orders != null) {
                totalOrders += orders;
            }
        }
        
        return totalOrders.intValue();
    }
    
    public BigDecimal getAverageOrderPrice(String period) {
        BigDecimal totalRevenue = getTotalRevenue(period);
        Integer totalOrders = getTotalOrders(period);
        
        if (totalOrders == 0) return BigDecimal.ZERO;
        return totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
    }
    
    public List<Map<String, Object>> getHotelCommentStats() {
        List<HotelComment> allComments = commentRepository.findAll();
        
        List<Hotel> allHotels = hotelRepository.findAll();
        Map<Long, String> hotelNameMap = allHotels.stream()
                .collect(Collectors.toMap(Hotel::getId, Hotel::getName, (a, b) -> a));
        
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
    
    public List<BigDecimal> getMonthlyRevenue(int year) {
        List<BigDecimal> revenues = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = getRevenueByMonth(year, month);
            revenues.add(revenue != null ? revenue : BigDecimal.ZERO);
        }
        return revenues;
    }
    
    public List<Long> getMonthlyOrders(int year) {
        List<Long> orders = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Long count = countOrdersByMonth(year, month);
            orders.add(count != null ? count : 0L);
        }
        return orders;
    }
    
    public List<BigDecimal> getMonthlyOccupancy(int year) {
        List<BigDecimal> occupancies = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal occupancy = calculateMonthlyOccupancy(year, month);
            occupancies.add(occupancy != null ? occupancy : BigDecimal.ZERO);
        }
        return occupancies;
    }
    
    // ========== 辅助方法 ==========
    
    private BigDecimal getAveragePriceByMonth(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<HotelOrder> allOrders = orderRepository.findAll();
        
        List<HotelOrder> monthOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null)
                .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                .filter(o -> "已完成".equals(o.getStatus()) || "已入住".equals(o.getStatus()))
                .collect(Collectors.toList());
        
        if (monthOrders.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal total = BigDecimal.ZERO;
        for (HotelOrder order : monthOrders) {
            Double price = order.getPrice();
            if (price != null) {
                total = total.add(BigDecimal.valueOf(price));
            }
        }
        
        return total.divide(BigDecimal.valueOf(monthOrders.size()), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateMonthlyOccupancy(int year, int month) {
        long totalRooms = roomTypeRepository.count();
        if (totalRooms == 0) return BigDecimal.ZERO;
        
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        List<HotelOrder> allOrders = orderRepository.findAll();
        
        long occupiedRooms = allOrders.stream()
                .filter(o -> o.getCreateTime() != null)
                .filter(o -> o.getCreateTime().isAfter(start) && o.getCreateTime().isBefore(end))
                .filter(o -> "已入住".equals(o.getStatus()) || "已完成".equals(o.getStatus()))
                .count();
        
        BigDecimal occupancy = BigDecimal.valueOf(occupiedRooms)
                .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return occupancy.setScale(2, RoundingMode.HALF_UP);
    }
    
    private int getMonths(String period) {
        if ("12months".equals(period)) return 12;
        if ("3months".equals(period)) return 3;
        return 6;
    }
}