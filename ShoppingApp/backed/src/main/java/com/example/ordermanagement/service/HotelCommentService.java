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

    public Double getAvgScoreByHotelId(Long hotelId) {
        Double avg = hotelCommentRepository.getAvgScoreByHotelId(hotelId);
        return avg != null ? avg : 0.0;
    }

    public Integer getCommentCountByHotelId(Long hotelId) {
        Integer count = hotelCommentRepository.getCommentCountByHotelId(hotelId);
        return count != null ? count : 0;
    }

    public List<HotelComment> getMerchantComments(List<Long> hotelIds) {
        return hotelCommentRepository.findByHotelIds(hotelIds);
    }

    public List<HotelComment> getMerchantCommentsByRating(List<Long> hotelIds, Integer rating) {
        return hotelCommentRepository.findByHotelIdsAndRating(hotelIds, rating);
    }

    public boolean updateStatus(Long id, String status) {
        return hotelCommentRepository.updateStatus(id, status) > 0;
    }

    public boolean updateReply(Long id, String reply) {
        return hotelCommentRepository.updateReply(id, reply) > 0;
    }

    public HotelComment getById(Long id) {
        return hotelCommentRepository.findById(id).orElse(null);
    }
}