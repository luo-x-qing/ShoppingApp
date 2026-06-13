package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.service.ScenicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public ResponseEntity<Scenic> getById(@PathVariable Long id) {
        Scenic scenic = scenicService.getById(id);
        return scenic != null ? ResponseEntity.ok(scenic) : ResponseEntity.notFound().build();
    }

    @PostMapping("/add")
    public ResponseEntity<Scenic> add(@RequestBody Scenic scenic) {
        scenic.setAuditStatus(0);
        return ResponseEntity.ok(scenicService.save(scenic));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scenic> update(@PathVariable Long id, @RequestBody Scenic scenic) {
        Scenic updated = scenicService.update(id, scenic);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scenicService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Scenic>> search(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(scenicService.search(province, city, name));
    }

}
