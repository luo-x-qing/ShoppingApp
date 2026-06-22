package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelImage;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.service.BaiduMapService;
import com.example.ordermanagement.service.HotelService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private BaiduMapService baiduMapService;

    @GetMapping
    public Result<List<Hotel>> getAllHotels() {
        try {
            List<Hotel> hotels = hotelService.getAllHotels();
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Hotel> getHotelById(@PathVariable Long id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel != null) {
                return Result.success(hotel);
            } else {
                return Result.error("酒店不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getHotelDetail(@PathVariable Long id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            if (hotel == null) {
                return Result.error("酒店不存在");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("hotel", hotel);
            result.put("roomTypes", hotelService.getRoomTypesByHotel(id));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Hotel> saveHotel(@RequestBody Hotel hotel) {
        try {
            if (hotel.getDetailImages() != null) {
                for (HotelImage img : hotel.getDetailImages()) {
                    img.setHotel(hotel);
                }
            }
            Hotel saved = hotelService.saveHotel(hotel);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Hotel> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        try {
            Hotel existing = hotelService.getHotelById(id);
            if (existing == null) {
                return Result.error("酒店不存在");
            }
            existing.setName(hotel.getName());
            existing.setCategory(hotel.getCategory());
            existing.setAddress(hotel.getAddress());
            existing.setStarLevel(hotel.getStarLevel());
            existing.setPrice(hotel.getPrice());
            existing.setLatitude(hotel.getLatitude());
            existing.setLongitude(hotel.getLongitude());
            existing.setTotalRooms(hotel.getTotalRooms());
            existing.setMerchantId(hotel.getMerchantId());
            existing.setStatus(hotel.getStatus());

            if (hotel.getCoverImage() != null) {
                existing.setCoverImage(hotel.getCoverImage());
            }

            existing.getDetailImages().clear();
            if (hotel.getDetailImages() != null) {
                for (HotelImage img : hotel.getDetailImages()) {
                    img.setHotel(existing);
                    existing.getDetailImages().add(img);
                }
            }

            Hotel updated = hotelService.saveHotel(existing);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public Result<List<Hotel>> getHotelsByCategory(@PathVariable String category) {
        try {
            List<Hotel> hotels = hotelService.getHotelsByCategory(category);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/sort/price")
    public Result<List<Hotel>> sortByPrice() {
        try {
            List<Hotel> hotels = hotelService.getHotelsOrderByPrice();
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/sort/rating")
    public Result<List<Hotel>> sortByRating() {
        try {
            List<Hotel> hotels = hotelService.getHotelsOrderByRating();
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/sort/distance")
    public Result<List<Hotel>> sortByDistance(@RequestParam double lat, @RequestParam double lng) {
        try {
            List<Hotel> hotels = hotelService.getHotelsOrderByDistance(lat, lng);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<Hotel>> searchHotels(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category) {
        try {
            List<Hotel> hotels = hotelService.searchHotels(keyword, category);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public Result<List<Hotel>> getRecommendations(@RequestParam String username) {
        try {
            List<Hotel> hotels = hotelService.getPersonalizedRecommendations(username);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<Hotel>> getHotelsByMerchant(@PathVariable Long merchantId) {
        try {
            List<Hotel> hotels = hotelService.getHotelsByMerchant(merchantId);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{hotelId}/room-types")
    public Result<List<RoomType>> getRoomTypes(@PathVariable Long hotelId) {
        try {
            List<RoomType> roomTypes = hotelService.getRoomTypesByHotel(hotelId);
            return Result.success(roomTypes);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/room-types")
    public Result<RoomType> addRoomType(@RequestBody RoomType roomType) {
        try {
            RoomType saved = hotelService.addRoomType(roomType);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/room-types/{id}")
    public Result<RoomType> updateRoomType(@PathVariable Long id, @RequestBody RoomType roomType) {
        try {
            roomType.setId(id);
            RoomType updated = hotelService.updateRoomType(roomType);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/room-types/{id}")
    public Result<Map<String, Object>> deleteRoomType(@PathVariable Long id) {
        try {
            hotelService.deleteRoomType(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/nearby-by-address")
    public Result<List<Hotel>> getNearbyHotelsByAddress(@RequestParam String address) {
        try {
            List<Hotel> hotels = hotelService.getHotelsSortedByAddress(address);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/address-recognize")
    public Result<Map<String, String>> recognizeAddress(@RequestParam String address) {
        try {
            Map<String, String> result = new HashMap<>();
            String recognized = hotelService.smartAddressRecognize(address);
            result.put("original", address);
            result.put("recognized", recognized);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("识别失败：" + e.getMessage());
        }
    }

    @PostMapping("/update-coordinates")
    public Result<Map<String, Object>> updateHotelCoordinates() {
        try {
            List<Hotel> hotels = hotelService.getAllHotels();
            int successCount = 0;

            for (Hotel hotel : hotels) {
                if (hotel.getAddress() != null && !hotel.getAddress().isEmpty()) {
                    try {
                        JsonNode geoResult = baiduMapService.geocode(hotel.getAddress());
                        if (geoResult != null && geoResult.has("status") && geoResult.get("status").asInt() == 0) {
                            JsonNode location = geoResult.get("result").get("location");
                            double lat = location.get("lat").asDouble();
                            double lng = location.get("lng").asDouble();
                            hotel.setLatitude(lat);
                            hotel.setLongitude(lng);
                            hotelService.saveHotel(hotel);
                            successCount++;
                        } else {
                            String message = geoResult.has("message") ? geoResult.get("message").asText() : "解析失败";
                            System.out.println("更新酒店失败: " + hotel.getName() + " -> " + message);
                        }
                    } catch (Exception e) {
                        System.err.println("更新酒店坐标异常: " + hotel.getName() + " - " + e.getMessage());
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("total", hotels.size());
            response.put("success", successCount);
            response.put("message", "成功更新 " + successCount + " 个酒店的坐标");
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

}
