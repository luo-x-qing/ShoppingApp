package com.example.demo.controller;

import com.example.ordermanagement.service.AttractionService;
import com.example.ordermanagement.service.HotelService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @Autowired
    private HotelService hotelService;
    @Autowired
    private AttractionService attractionService;
    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String adminIndex() {
        return "admin/index";
    }

    @GetMapping("/admin/hotels")
    public String hotelPage(Model model) {
        model.addAttribute("hotels", hotelService.getAllHotels());
        return "admin/hotel";
    }

    @GetMapping("/admin/attractions")
    public String attractionPage(Model model) {
        model.addAttribute("attractions", attractionService.getAllAttractions());
        return "admin/attraction";
    }

    @GetMapping("/admin/users")
    public String userPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user";
    }
}
