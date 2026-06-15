package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.service.HotelCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel-comments")
public class HotelCommentController {

    @Autowired
    private HotelCommentService hotelCommentService;

    @GetMapping("/hotel/{hotelId}")
    public List<HotelComment> getByHotel(@PathVariable Long hotelId) {
        return hotelCommentService.getByHotelId(hotelId);
    }

    @PostMapping
    public String create(@RequestBody HotelComment comment) {
        if (hotelCommentService.existsByOrderId(comment.getOrderId())) {
            return "已评价";
        }
        hotelCommentService.save(comment);
        return "success";
    }

    @GetMapping("/all")
    public List<HotelComment> getAllComments() {
        return hotelCommentService.getAllComments();
    }

    @GetMapping("/merchant")
    public List<HotelComment> getMerchantComments(@RequestParam List<Long> hotelIds) {
        return hotelCommentService.getMerchantComments(hotelIds);
    }

    @GetMapping("/merchant/rating")
    public List<HotelComment> getMerchantCommentsByRating(
            @RequestParam List<Long> hotelIds,
            @RequestParam Integer rating) {
        return hotelCommentService.getMerchantCommentsByRating(hotelIds, rating);
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<Map<String, Object>> hideComment(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelCommentService.updateStatus(id, "隐藏"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/show")
    public ResponseEntity<Map<String, Object>> showComment(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelCommentService.updateStatus(id, "正常"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> replyComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelCommentService.updateReply(id, body.get("reply")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelComment> getById(@PathVariable Long id) {
        HotelComment comment = hotelCommentService.getById(id);
        return comment != null ? ResponseEntity.ok(comment) : ResponseEntity.notFound().build();
    }
}