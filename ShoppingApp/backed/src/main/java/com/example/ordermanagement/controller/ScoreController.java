package com.example.ordermanagement.controller;
import com.example.ordermanagement.model.Score;
import com.example.ordermanagement.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score")
public class ScoreController {
    @Autowired private ScoreService scoreService;
    @PostMapping
    public ResponseEntity<Score> addScore(@RequestBody Score s){
        return ResponseEntity.ok(scoreService.save(s));
    }
    @GetMapping("/avg/{aid}")
    public ResponseEntity<Double> getAvg(@PathVariable Long aid){
        return ResponseEntity.ok(scoreService.getAvg(aid));
    }
}