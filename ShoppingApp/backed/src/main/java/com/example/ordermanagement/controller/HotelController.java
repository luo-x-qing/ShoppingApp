package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelImage;
import com.example.ordermanagement.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id);
        return hotel != null ? ResponseEntity.ok(hotel) : ResponseEntity.notFound().build();
    }

    // 新增酒店（支持地址）
    @PostMapping
    public ResponseEntity<Hotel> saveHotel(@RequestBody Hotel hotel) {
        // 绑定详情图和酒店的关系
        if (hotel.getDetailImages() != null) {
            for (HotelImage img : hotel.getDetailImages()) {
                img.setHotel(hotel);
            }
        }
        return ResponseEntity.ok(hotelService.saveHotel(hotel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // 修改酒店（支持地址 + 支持详情图完全更新）
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> updateHotel(
            @PathVariable Long id,
            @RequestBody Hotel hotel) {
        Hotel existing = hotelService.getHotelById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // 关键：把前端传过来的 地址、名称、分类、星级、价格 全部更新
        existing.setName(hotel.getName());
        existing.setCategory(hotel.getCategory());
        existing.setAddress(hotel.getAddress()); // 🔥 地址保存
        existing.setStarLevel(hotel.getStarLevel());
        existing.setPrice(hotel.getPrice());

        // 如果传了新封面图，也更新
        if (hotel.getCoverImage() != null) {
            existing.setCoverImage(hotel.getCoverImage());
        }

        // 清空旧详情图，使用新的详情图
        existing.getDetailImages().clear();
        if (hotel.getDetailImages() != null) {
            for (HotelImage img : hotel.getDetailImages()) {
                img.setHotel(existing);
                existing.getDetailImages().add(img);
            }
        }

        return ResponseEntity.ok(hotelService.saveHotel(existing));
    }

    @GetMapping("/category/{category}")
    public List<Hotel> getHotelsByCategory(@PathVariable String category) {
        return hotelService.getHotelsByCategory(category);
    }

    @GetMapping("/merchant/{merchantId}")
    public List<Hotel> getHotelsByMerchant(@PathVariable Long merchantId) {
        return hotelService.getHotelsByMerchant(merchantId);
    }

    @PostMapping("/{id}/room-types")
    public ResponseEntity<Hotel> addRoomType(@PathVariable Long id, @RequestBody com.example.ordermanagement.model.RoomType roomType) {
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel == null) return ResponseEntity.notFound().build();
        hotelService.addRoomType(hotel, roomType);
        return ResponseEntity.ok(hotelService.saveHotel(hotel));
    }

    @PutMapping("/{hotelId}/room-types/{roomTypeId}")
    public ResponseEntity<Hotel> updateRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId, @RequestBody com.example.ordermanagement.model.RoomType roomType) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) return ResponseEntity.notFound().build();
        hotelService.updateRoomType(hotel, roomTypeId, roomType);
        return ResponseEntity.ok(hotelService.saveHotel(hotel));
    }

    @DeleteMapping("/{hotelId}/room-types/{roomTypeId}")
    public ResponseEntity<Hotel> deleteRoomType(@PathVariable Long hotelId, @PathVariable Long roomTypeId) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) return ResponseEntity.notFound().build();
        hotelService.deleteRoomType(hotel, roomTypeId);
        return ResponseEntity.ok(hotelService.saveHotel(hotel));
    }
}