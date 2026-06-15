package com.example.ordermanagement.service;

import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomTypeService {
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    public List<RoomType> findByHotelId(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }
    
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }
    
    public RoomType findById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public RoomType update(RoomType roomType) {
        if (!roomTypeRepository.existsById(roomType.getId())) {
            return null;
        }
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public boolean deleteById(Long id) {
        if (roomTypeRepository.existsById(id)) {
            roomTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Transactional
    public int decreaseAvailableCount(Long id, Integer count) {
        return roomTypeRepository.decreaseAvailableCount(id, count);
    }
    
    @Transactional
    public int increaseAvailableCount(Long id, Integer count) {
        return roomTypeRepository.increaseAvailableCount(id, count);
    }
}
