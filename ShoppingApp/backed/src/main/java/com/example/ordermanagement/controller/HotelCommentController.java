package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelCommentService;
import com.example.ordermanagement.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotel-comments")
@CrossOrigin(origins = "*")
public class HotelCommentController {

    @Autowired
    private HotelCommentService hotelCommentService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotel/{hotelId}")
    public Result<List<HotelComment>> getByHotel(@PathVariable Long hotelId) {
        try {
            List<HotelComment> comments = hotelCommentService.getByHotelId(hotelId)
                    .stream()
                    .filter(c -> !"违规".equals(c.getStatus()))
                    .collect(Collectors.toList());
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody HotelComment comment) {
        Map<String, Object> result = new HashMap<>();

        if (hotelCommentService.existsByOrderId(comment.getOrderId())) {
            result.put("success", false);
            result.put("message", "已评价");
            return Result.error("已评价");
        }

        hotelCommentService.save(comment);
        result.put("success", true);
        result.put("message", "评价成功");
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<HotelComment>> getAllComments() {
        try {
            List<HotelComment> comments = hotelCommentService.getAllComments();
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<HotelComment> getById(@PathVariable Long id) {
        try {
            HotelComment comment = hotelCommentService.getById(id);
            if (comment != null) {
                return Result.success(comment);
            } else {
                return Result.error("评价不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    // ========== 商家评价管理接口 ==========

    @GetMapping("/merchant")
    public Result<List<HotelComment>> getMerchantComments(
            @RequestParam Long merchantId,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Integer rating) {
        try {
            List<Hotel> hotels = hotelService.getHotelsByMerchant(merchantId);
            if (hotels.isEmpty()) {
                return Result.success(java.util.Collections.emptyList());
            }

            List<Long> hotelIds;
            if (hotelId != null && hotelId > 0) {
                hotelIds = java.util.Arrays.asList(hotelId);
            } else {
                hotelIds = hotels.stream()
                        .map(Hotel::getId)
                        .collect(java.util.stream.Collectors.toList());
            }

            List<HotelComment> comments = hotelCommentService.getMerchantComments(hotelIds, rating);
            return Result.success(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/{commentId}/reply")
    public Result<Map<String, Object>> replyComment(@PathVariable Long commentId, @RequestBody Map<String, String> params) {
        try {
            String reply = params.get("reply");
            boolean success = hotelCommentService.replyComment(commentId, reply);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);

            if (success) {
                return Result.success(result);
            } else {
                return Result.error("回复失败");
            }
        } catch (Exception e) {
            return Result.error("回复失败：" + e.getMessage());
        }
    }

    @PutMapping("/{commentId}/reply")
    public Result<Map<String, Object>> updateReply(@PathVariable Long commentId, @RequestBody Map<String, String> params) {
        try {
            String reply = params.get("reply");
            boolean success = hotelCommentService.updateReply(commentId, reply);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);

            if (success) {
                return Result.success(result);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/hide")
    public Result<Map<String, Object>> hideComment(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelCommentService.updateStatus(id, "隐藏"));
        return Result.success(result);
    }

    @PostMapping("/{id}/show")
    public Result<Map<String, Object>> showComment(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", hotelCommentService.updateStatus(id, "正常"));
        return Result.success(result);
    }

    // ========== 管理员：获取违规评价 ==========
    @GetMapping("/violations")
    public Result<List<HotelComment>> getViolations() {
        try {
            List<HotelComment> comments = hotelCommentService.getViolationComments();
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    // ========== 管理员：标记违规 ==========
    @PostMapping("/{commentId}/violation")
    public Result<Map<String, Object>> markViolation(@PathVariable Long commentId) {
        try {
            boolean success = hotelCommentService.markViolation(commentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return success ? Result.success(result) : Result.error("标记失败");
        } catch (Exception e) {
            return Result.error("标记失败：" + e.getMessage());
        }
    }

    // ========== 管理员：删除评价 ==========
    @DeleteMapping("/{commentId}")
    public Result<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        try {
            hotelCommentService.deleteComment(commentId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
