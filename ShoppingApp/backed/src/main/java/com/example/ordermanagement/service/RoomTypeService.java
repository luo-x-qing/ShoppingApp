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

    /**
     * 获取酒店的所有房型
     */
    public List<RoomType> findByHotelId(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    /**
     * 获取所有房型
     */
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    /**
     * 根据ID获取房型
     */
    public RoomType findById(Long id) {
        return roomTypeRepository.findById(id).orElse(null);
    }

    /**
     * 保存房型
     */
    @Transactional
    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    /**
     * 更新房型
     */
    @Transactional
    public RoomType update(RoomType roomType) {
        if (!roomTypeRepository.existsById(roomType.getId())) {
            return null;
        }
        return roomTypeRepository.save(roomType);
    }

    /**
     * 删除房型
     */
    @Transactional
    public boolean deleteById(Long id) {
        if (roomTypeRepository.existsById(id)) {
            roomTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 扣减库存
     */
    @Transactional
    public int decreaseAvailableCount(Long id, Integer count) {
        return roomTypeRepository.decreaseAvailableCount(id, count);
    }

    /**
     * 恢复库存
     */
    @Transactional
    public int increaseAvailableCount(Long id, Integer count) {
        return roomTypeRepository.increaseAvailableCount(id, count);
    }
}
