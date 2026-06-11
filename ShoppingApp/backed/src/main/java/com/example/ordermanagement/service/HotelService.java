package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

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
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    // ====================== 新增：按分类查询 ======================
    public List<Hotel> getHotelsByCategory(String category) {
        return hotelRepository.findByCategory(category);
    }
}