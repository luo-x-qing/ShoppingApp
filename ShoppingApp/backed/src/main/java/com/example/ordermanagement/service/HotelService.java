package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.HotelCommentRepository;
import com.example.ordermanagement.repository.HotelOrderRepository;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    @Autowired
    private BaiduMapService baiduMapService;

    @Autowired
    private HotelCommentRepository hotelCommentRepository;  // 新增：注入评价仓库

    // 修改：获取所有酒店并计算平均评分
    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();

        // 为每个酒店计算平均评分和评价数量
        for (Hotel hotel : hotels) {
            // 计算平均评分
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);

            // 可选：也可以设置评价数量（如果需要）
            // Integer commentCount = hotelCommentRepository.getCommentCountByHotelId(hotel.getId());
            // hotel.setReviewCount(commentCount != null ? commentCount : 0);
        }

        return hotels;
    }

    public Hotel getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElse(null);
        if (hotel != null) {
            // 获取平均评分
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }
        return hotel;
    }

    public Hotel saveHotel(Hotel hotel) {
        if (hotel.getDetailImages() != null) {
            hotel.getDetailImages().forEach(image -> image.setHotel(hotel));
        }
        if (hotel.getRoomTypes() != null) {
            hotel.getRoomTypes().forEach(room -> room.setHotel(hotel));
        }
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    public List<Hotel> getHotelsByCategory(String category) {
        List<Hotel> hotels = hotelRepository.findByCategory(category);

        // 为每个酒店计算平均评分
        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    // ========== 商家端方法 ==========
    public List<Hotel> getHotelsByMerchant(Long merchantId) {
        List<Hotel> hotels = hotelRepository.findByMerchantId(merchantId);

        // 为每个酒店计算平均评分
        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    // ========== 排序方法 ==========
    public List<Hotel> getHotelsOrderByPrice() {
        List<Hotel> hotels = hotelRepository.findAllOrderByPriceAsc();

        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    public List<Hotel> getHotelsOrderByRating() {
        List<Hotel> hotels = hotelRepository.findAllOrderByRatingDesc();

        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    public List<Hotel> getHotelsOrderByDistance(double lat, double lng) {
        List<Hotel> hotels = hotelRepository.findAllOrderByDistance(lat, lng);

        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    // ========== 个性化推荐 ==========
    public List<Hotel> getPersonalizedRecommendations(String username) {
        List<HotelOrder> userOrders = hotelOrderRepository.findByUsernameOrderByIdDesc(username);

        if (userOrders.isEmpty()) {
            return hotelRepository.findAllOrderByRatingDesc().stream().limit(10).collect(Collectors.toList());
        }

        List<String> categories = userOrders.stream()
                .map(order -> {
                    Hotel hotel = getHotelById(order.getHotelId());
                    return hotel != null ? hotel.getCategory() : null;
                })
                .filter(cat -> cat != null)
                .distinct()
                .collect(Collectors.toList());

        double avgPrice = userOrders.stream().mapToDouble(HotelOrder::getPrice).average().orElse(500);
        double minPrice = avgPrice * 0.7;
        double maxPrice = avgPrice * 1.5;

        List<Hotel> hotels = hotelRepository.findRecommendedHotels(categories, minPrice, maxPrice);

        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    // ========== 房间类型管理 ==========
    public RoomType addRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    public RoomType updateRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    public void deleteRoomType(Long roomTypeId) {
        roomTypeRepository.deleteById(roomTypeId);
    }

    public List<RoomType> getRoomTypesByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    public List<Hotel> getHotelsSortedByAddress(String address) {
        JsonNode geoResult = baiduMapService.geocode(address);

        if (geoResult == null) {
            throw new RuntimeException("地址识别失败，请检查地址是否正确");
        }

        if (geoResult.has("status") && geoResult.get("status").asInt() == 0) {
            JsonNode location = geoResult.get("result").get("location");
            double userLat = location.get("lat").asDouble();
            double userLng = location.get("lng").asDouble();
            List<Hotel> hotels = hotelRepository.findAllOrderByDistance(userLat, userLng);

            for (Hotel hotel : hotels) {
                Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
                hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
            }

            return hotels;
        } else {
            String message = geoResult.has("message") ? geoResult.get("message").asText() : "解析失败";
            throw new RuntimeException("地址识别失败: " + message);
        }
    }

    public String smartAddressRecognize(String inputAddress) {
        JsonNode result = baiduMapService.geocode(inputAddress);
        if (result != null && result.has("status") && result.get("status").asInt() == 0) {
            if (result.has("result") && result.get("result").has("formatted_address")) {
                return result.get("result").get("formatted_address").asText();
            }
        }
        return inputAddress;
    }

    public JsonNode searchNearbyPoi(String hotelName, String poiType, String region) {
        Hotel hotel = hotelRepository.findByName(hotelName);
        if (hotel == null || hotel.getLatitude() == null) {
            return null;
        }
        String query = poiType;
        String location = hotel.getLatitude() + "," + hotel.getLongitude();
        return baiduMapService.searchNearbyPoi(query, location);
    }

    /**
     * 模糊搜索酒店
     */
    public List<Hotel> searchHotels(String keyword, String category) {
        List<Hotel> hotels = hotelRepository.searchByKeyword(keyword, category);

        // 为每个酒店计算平均评分
        for (Hotel hotel : hotels) {
            Double avgRating = hotelCommentRepository.getAvgScoreByHotelId(hotel.getId());
            hotel.setAvgRating(avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);
        }

        return hotels;
    }

    // ========== 🔥 新增：统计方法（供 AdminStatisticsController 调用） ==========

    /**
     * 统计所有酒店数量
     */
    public long countAll() {
        return hotelRepository.count();
    }

    /**
     * 按状态统计酒店数量
     * @param status 状态：pending(待审核), approved(已通过), violation(违规)
     */
    public long countByStatus(String status) {
        List<Hotel> hotels = hotelRepository.findAll();
        return hotels.stream()
                .filter(h -> h.getStatus() != null && h.getStatus().equals(status))
                .count();
    }
}