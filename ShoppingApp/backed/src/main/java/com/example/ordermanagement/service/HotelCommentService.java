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

    // 敏感词检测（简单实现，可替换为百度AI接口）
    private boolean containsSensitiveWords(String content) {
        String[] sensitiveWords = {"敏感词1", "敏感词2"}; // 扩展
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
        // 敏感词过滤
        if (containsSensitiveWords(comment.getContent())) {
            comment.setStatus("违规");
        }

        HotelComment saved = hotelCommentRepository.save(comment);

        // 更新酒店平均评分
        updateHotelAverageRating(comment.getHotelId());

        return saved;
    }

    public List<HotelComment> getAllComments() {
        return hotelCommentRepository.findAll();
    }

    // ========== 商家评价管理 ==========

    /**
     * 获取商家酒店的所有评价
     * @param hotelIds 商家的酒店ID列表
     * @param rating 评分筛选（可选）
     * @return 评价列表
     */
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

    /**
     * 商家回复评价
     * @param commentId 评价ID
     * @param reply 回复内容
     * @return 是否成功
     */
    @Transactional
    public boolean replyComment(Long commentId, String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return false;
        }

        HotelComment comment = hotelCommentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return false;
        }

        // 敏感词过滤
        if (containsSensitiveWords(reply)) {
            return false;
        }

        int updated = hotelCommentRepository.updateReply(commentId, reply);
        return updated > 0;
    }

    /**
     * 商家修改回复
     * @param commentId 评价ID
     * @param reply 新回复内容
     * @return 是否成功
     */
    @Transactional
    public boolean updateReply(Long commentId, String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return false;
        }

        HotelComment comment = hotelCommentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.getReply() == null) {
            return false;
        }

        // 敏感词过滤
        if (containsSensitiveWords(reply)) {
            return false;
        }

        int updated = hotelCommentRepository.updateReply(commentId, reply);
        return updated > 0;
    }

    /**
     * 更新酒店平均评分
     */
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

    // ========== 管理员功能 ==========

    // 管理员：获取违规评价
    public List<HotelComment> getViolationComments() {
        return hotelCommentRepository.findByStatus("违规");
    }

    // 管理员：标记为违规
    @Transactional
    public boolean markViolation(Long commentId) {
        int updated = hotelCommentRepository.updateStatus(commentId, "违规");
        return updated > 0;
    }

    // 管理员：删除评价
    @Transactional
    public void deleteComment(Long commentId) {
        hotelCommentRepository.deleteById(commentId);
    }
}