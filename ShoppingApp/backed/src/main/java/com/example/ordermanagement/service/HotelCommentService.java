package com.example.ordermanagement.service;

import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.repository.HotelCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HotelCommentService {

    @Autowired
    private HotelCommentRepository hotelCommentRepository;

    public List<HotelComment> getByHotelId(Long hotelId) {
        return hotelCommentRepository.findByHotelId(hotelId);
    }

    public boolean existsByOrderId(Long orderId) {
        return hotelCommentRepository.existsByOrderId(orderId);
    }

    public HotelComment save(HotelComment comment) {
        return hotelCommentRepository.save(comment);
    }

    public List<HotelComment> getAllComments() {
        return hotelCommentRepository.findAll();
    }
}