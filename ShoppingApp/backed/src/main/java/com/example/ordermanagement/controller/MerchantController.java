package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotels")
    public Result<List<Hotel>> getMerchantHotels(@RequestParam Long merchantId) {
        try {
            List<Hotel> hotels = hotelService.getHotelsByMerchant(merchantId);
            return Result.success(hotels);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
