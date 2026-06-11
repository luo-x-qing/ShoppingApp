package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.model.Comment;
import com.example.ordermanagement.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AttractionService attractionService;

    public List<Comment> getCommentsByAttractionId(Long attractionId) {
        return commentRepository.findByAttractionIdOrderByCreateTimeDesc(attractionId);
    }

    // 保存评论后自动重新计算平均分
    public Comment saveComment(Comment comment) {
        Comment saved = commentRepository.save(comment);
        calculateAndUpdateScore(comment.getAttractionId());
        return saved;
    }

    // 👇 自动计算平均分并更新景点
    private void calculateAndUpdateScore(Long attractionId) {
        List<Comment> comments = getCommentsByAttractionId(attractionId);
        if (comments.isEmpty()) return;

        double total = 0;
        for (Comment c : comments) {
            total += c.getScore();
        }
        double avg = total / comments.size();

        Attraction attr = attractionService.getAttractionById(attractionId);
        attr.setScore(avg);
        attractionService.saveAttraction(attr);
    }
}