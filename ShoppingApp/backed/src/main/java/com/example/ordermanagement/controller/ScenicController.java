package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.service.ScenicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scenic")
public class ScenicController {

    private final ScenicService scenicService;

    public ScenicController(ScenicService scenicService) {
        this.scenicService = scenicService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Scenic>> list() {
        return ResponseEntity.ok(scenicService.list());
    }

    @PostMapping("/Scenicadd")
    public ResponseEntity<Scenic> add(@RequestBody Scenic scenic) {
        scenic.setAuditStatus(0);
        return ResponseEntity.ok(scenicService.save(scenic));
    }
}
