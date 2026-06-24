package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.McpProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*")
public class FlightSearchController {

    @Autowired
    private McpProxyService mcpProxyService;

    @GetMapping("/search")
    public Map<String, Object> searchFlights(
            @RequestParam String dep,
            @RequestParam String arr,
            @RequestParam String date) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            String data = mcpProxyService.searchFlights(dep, arr, date);
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}