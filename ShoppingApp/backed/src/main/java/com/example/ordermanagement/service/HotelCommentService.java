package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.repository.HotelCommentRepository;
import com.example.ordermanagement.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HotelCommentService {

    @Autowired
    private HotelCommentRepository hotelCommentRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private boolean containsSensitiveWords(String content) {
        String[] sensitiveWords = {"敏感词1", "敏感词2"};
        for (String word : sensitiveWords) {
            if (content.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public List<HotelComment> getByHotelId(Long hotelId) {
        return hotelCommentRepository.findByHotelId(hotelId);
    }

    public boolean existsByOrderId(Long orderId) {
        return hotelCommentRepository.existsByOrderId(orderId);
    }

    @Transactional
    public HotelComment save(HotelComment comment) {
        if (containsSensitiveWords(comment.getContent())) {
            comment.setStatus("违规");
        }

        HotelComment saved = hotelCommentRepository.save(comment);

        updateHotelAverageRating(comment.getHotelId());

        return saved;
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

    public List<HotelComment> getMerchantComments(List<Long> hotelIds, Integer rating) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        if (rating != null && rating > 0) {
            return hotelCommentRepository.findByHotelIdsAndRating(hotelIds, rating);
        } else {
            return hotelCommentRepository.findByHotelIds(hotelIds);
        }
    }

    @Transactional
    public boolean replyComment(Long commentId, String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return false;
        }

        HotelComment comment = hotelCommentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return false;
        }

        if (containsSensitiveWords(reply)) {
            return false;
        }

        int updated = hotelCommentRepository.updateReply(commentId, reply);
        return updated > 0;
    }

    @Transactional
    public boolean updateReply(Long commentId, String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return false;
        }

        HotelComment comment = hotelCommentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.getReply() == null) {
            return false;
        }

        if (containsSensitiveWords(reply)) {
            return false;
        }

        int updated = hotelCommentRepository.updateReply(commentId, reply);
        return updated > 0;
    }

    public boolean updateStatus(Long id, String status) {
        return hotelCommentRepository.updateStatus(id, status) > 0;
    }

    public HotelComment getById(Long id) {
        return hotelCommentRepository.findById(id).orElse(null);
    }

    private void updateHotelAverageRating(Long hotelId) {
        List<HotelComment> comments = hotelCommentRepository.findByHotelId(hotelId);
        double avg = comments.stream()
                .filter(c -> "正常".equals(c.getStatus()))
                .mapToInt(HotelComment::getScore)
                .average()
                .orElse(0.0);

        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel != null) {
            hotel.setAvgRating(Math.round(avg * 10) / 10.0);
            hotelRepository.save(hotel);
        }
    }

    public List<HotelComment> getViolationComments() {
        return hotelCommentRepository.findByStatus("违规");
    }

    @Transactional
    public boolean markViolation(Long commentId) {
        int updated = hotelCommentRepository.updateStatus(commentId, "违规");
        return updated > 0;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        hotelCommentRepository.deleteById(commentId);
    }
}
