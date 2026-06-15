package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElse(null);
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
        return hotelRepository.findByCategory(category);
    }

    public List<Hotel> getHotelsByMerchant(Long merchantId) {
        return hotelRepository.findByMerchantId(merchantId);
    }

    public List<Hotel> searchHotels(String keyword, String category) {
        return hotelRepository.searchByKeyword(keyword, category);
    }

    public List<Hotel> getHotelsOrderByPrice() {
        return hotelRepository.findAllOrderByPriceAsc();
    }

    public List<Hotel> getHotelsOrderByRating() {
        return hotelRepository.findAllOrderByRatingDesc();
    }

    public List<Hotel> getHotelsOrderByDistance(double lat, double lng) {
        return hotelRepository.findAllOrderByDistance(lat, lng);
    }

    public void addRoomType(Hotel hotel, RoomType roomType) {
        roomType.setHotel(hotel);
        hotel.getRoomTypes().add(roomType);
    }

    public RoomType updateRoomType(Hotel hotel, Long roomTypeId, RoomType updated) {
        for (RoomType rt : hotel.getRoomTypes()) {
            if (rt.getId().equals(roomTypeId)) {
                rt.setTypeName(updated.getTypeName());
                rt.setPrice(updated.getPrice());
                rt.setTotalCount(updated.getTotalCount());
                rt.setAvailableCount(updated.getAvailableCount());
                rt.setSize(updated.getSize());
                rt.setBedType(updated.getBedType());
                rt.setWindowStatus(updated.getWindowStatus());
                rt.setBreakfast(updated.getBreakfast());
                return rt;
            }
        }
        return null;
    }

    public void deleteRoomType(Hotel hotel, Long roomTypeId) {
        hotel.getRoomTypes().removeIf(rt -> rt.getId().equals(roomTypeId));
    }

    public RoomType getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }

    public List<RoomType> getRoomTypesByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }
}