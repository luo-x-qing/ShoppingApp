package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.service.HotelCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-comments")
public class HotelCommentController {

    @Autowired
    private HotelCommentService hotelCommentService;

    // 获取酒店评价
    @GetMapping("/hotel/{hotelId}")
    public List<HotelComment> getByHotel(@PathVariable Long hotelId) {
        return hotelCommentService.getByHotelId(hotelId);
    }

    // 提交评价（包含评分）
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
}