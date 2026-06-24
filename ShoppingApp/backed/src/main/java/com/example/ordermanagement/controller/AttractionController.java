package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AmapService amapService;

    // 查询附近景点（基于坐标计算距离）
    @GetMapping("/{id}/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearbyAttractions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") double radius) {
        List<Map<String, Object>> nearby = attractionService.getNearbyAttractions(id, radius);
        return ResponseEntity.ok(nearby);
    }

    // 查询所有景点
    @GetMapping
    public List<Attraction> getAllAttractions() {
        return attractionService.getAllAttractions();
    }

    // 根据ID查询景点
    @GetMapping("/{id}")
    public ResponseEntity<Attraction> getAttractionById(@PathVariable Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        if (attraction != null) {
            return ResponseEntity.ok(attraction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 按省份查询景点
    @GetMapping("/province/{province}")
    public List<Attraction> getAttractionsByProvince(@PathVariable String province) {
        return attractionService.getAttractionsByProvince(province);
    }

    // 按城市查询景点
    @GetMapping("/city/{city}")
    public List<Attraction> getAttractionsByCity(@PathVariable String city) {
        return attractionService.getAttractionsByCity(city);
    }

    // 按省份和城市查询景点
    @GetMapping("/province/{province}/city/{city}")
    public List<Attraction> getAttractionsByProvinceAndCity(
            @PathVariable String province, @PathVariable String city) {
        return attractionService.getAttractionsByProvinceAndCity(province, city);
    }

    // 新增景点
    @PostMapping
    public ResponseEntity<Attraction> saveAttraction(@RequestBody Attraction attraction) {
        return ResponseEntity.ok(attractionService.saveAttraction(attraction));
    }

    // 修改景点
    @PutMapping("/{id}")
    public ResponseEntity<Attraction> updateAttraction(
            @PathVariable Long id,
            @RequestBody Attraction a) {
        a.setId(id);
        return ResponseEntity.ok(attractionService.saveAttraction(a));
    }

    // 删除景点
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.noContent().build();
    }

    // 搜索景点（支持省份或城市）
    @GetMapping("/search")
    public List<Attraction> searchAttractions(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam String name){
        if (city != null && !city.isEmpty()) {
            return attractionService.searchByCityAndName(city, name);
        }
        return attractionService.searchByProvinceAndName(province, name);
    }

    // 👇 新增：更新景区评分（前端自动调用）
    @PutMapping("/{id}/score")
    public ResponseEntity<Attraction> updateScore(
            @PathVariable Long id,
            @RequestBody Attraction attraction) {
        Attraction exist = attractionService.getAttractionById(id);
        exist.setScore(attraction.getScore());
        return ResponseEntity.ok(attractionService.saveAttraction(exist));
    }

    // 清空所有景点并重置ID编号（从1开始）
    @PostMapping("/reset-and-renumber")
    public ResponseEntity<Map<String, Object>> resetAndRenumber() {
        attractionService.resetAndRenumber();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("message", "重新编号成功，同省同市景点已连续编号");
        return ResponseEntity.ok(result);
    }

    // 自动补全所有景点的城市信息（通过高德API）
    @PostMapping("/auto-fill-city")
    public ResponseEntity<Map<String, Object>> autoFillCity() {
        int count = attractionService.autoFillCity();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("updated", count);
        result.put("message", "成功更新 " + count + " 个景点的城市信息");
        return ResponseEntity.ok(result);
    }

    // 测试地理编码
    @GetMapping("/test-geo")
    public ResponseEntity<Map<String, String>> testGeo(@RequestParam String address) {
        Map<String, String> result = amapService.geoDetail(address);
        return ResponseEntity.ok(result);
    }

    // 用 HttpURLConnection 测试
    @GetMapping("/test-raw")
    public ResponseEntity<String> testRaw(@RequestParam(defaultValue = "故宫博物院") String address) {
        String result = amapService.testRawGeocode(address);
        return ResponseEntity.ok(result != null ? result : "null");
    }

    // 从JSON文件导入全国景点（使用地理编码API）
    @PostMapping("/import-all")
    public ResponseEntity<Map<String, Object>> importFromJson() {
        int added = attractionService.importScenicSpotsFromJson();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("added", added);
        result.put("message", "成功导入 " + added + " 个景点");
        return ResponseEntity.ok(result);
    }

    // 直接传入景点数组导入（测试用）
    @PostMapping("/import-direct")
    public ResponseEntity<Map<String, Object>> importDirect(@RequestBody List<Map<String, String>> spots) {
        String[][] spotArray = spots.stream()
            .map(s -> new String[]{s.get("name"), s.get("province"), s.get("city")})
            .toArray(String[][]::new);
        int added = attractionService.importScenicSpotsDirect(spotArray);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("added", added);
        return ResponseEntity.ok(result);
    }

    // 重新对所有景点进行地理编码（带省份城市）
    // 单景点地理编码（前端 fallback 使用 Place API）
    @GetMapping("/{id}/geocode")
    public ResponseEntity<Map<String, Object>> geocodeAttraction(@PathVariable Long id) {
        Map<String, Double> coord = attractionService.geocodeAttraction(id);
        if (coord != null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("latitude", coord.get("lat"));
            result.put("longitude", coord.get("lng"));
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/re-geocode")
    public ResponseEntity<Map<String, Object>> reGeocode() {
        int updated = attractionService.reGeocodeAll();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("updated", updated);
        result.put("message", "成功重新编码 " + updated + " 个景点坐标");
        return ResponseEntity.ok(result);
    }

    // 精确重编码：在所有结果中优先选择 level 为"景点""风景区"等的坐标
    // 访问地址: http://localhost:8080/api/attractions/re-geocode-precise
    @GetMapping("/re-geocode-precise")
    public ResponseEntity<Map<String, Object>> reGeocodePrecise() {
        int updated = attractionService.reGeocodePrecise();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("updated", updated);
        result.put("message", "精确重编码完成，更新 " + updated + " 个景点");
        return ResponseEntity.ok(result);
    }
}
