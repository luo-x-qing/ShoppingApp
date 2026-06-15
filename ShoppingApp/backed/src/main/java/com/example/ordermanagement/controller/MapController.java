package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.BaiduMapService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/map")
@CrossOrigin
public class MapController {

    @Autowired
    private BaiduMapService baiduMapService;

    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestParam String address) {
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode result = baiduMapService.geocode(address);
            if (result != null && result.has("status")) {
                int status = result.get("status").asInt();
                if (status == 0) {
                    JsonNode location = result.get("result").get("location");
                    String formattedAddress = result.get("result").has("formatted_address") ?
                            result.get("result").get("formatted_address").asText() : address;
                    response.put("success", true);
                    response.put("latitude", location.get("lat").asDouble());
                    response.put("longitude", location.get("lng").asDouble());
                    response.put("address", formattedAddress);
                    return ResponseEntity.ok(response);
                } else {
                    String message = result.has("message") ? result.get("message").asText() : "解析失败";
                    response.put("success", false);
                    response.put("message", message);
                    return ResponseEntity.badRequest().body(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.put("success", false);
        response.put("message", "地址解析失败");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String, Object>> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode result = baiduMapService.reverseGeocode(lat, lng);
            if (result != null && result.has("status")) {
                int status = result.get("status").asInt();
                if (status == 0) {
                    JsonNode resultNode = result.get("result");
                    String address = resultNode.has("formatted_address") ?
                            resultNode.get("formatted_address").asText() : "";
                    response.put("success", true);
                    response.put("address", address);
                    response.put("latitude", lat);
                    response.put("longitude", lng);
                    List<Map<String, String>> nearbyPois = new ArrayList<>();
                    if (resultNode.has("pois") && resultNode.get("pois").isArray()) {
                        for (JsonNode poi : resultNode.get("pois")) {
                            Map<String, String> poiInfo = new HashMap<>();
                            poiInfo.put("name", poi.has("name") ? poi.get("name").asText() : "");
                            poiInfo.put("address", poi.has("address") ? poi.get("address").asText() : "");
                            nearbyPois.add(poiInfo);
                        }
                    }
                    if (nearbyPois.isEmpty() && !address.isEmpty()) {
                        Map<String, String> defaultPoi = new HashMap<>();
                        defaultPoi.put("name", "当前位置");
                        defaultPoi.put("address", address);
                        nearbyPois.add(defaultPoi);
                    }
                    response.put("nearbyPois", nearbyPois);
                    return ResponseEntity.ok(response);
                } else {
                    String message = result.has("message") ? result.get("message").asText() : "解析失败";
                    response.put("success", false);
                    response.put("message", message);
                    return ResponseEntity.badRequest().body(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.put("success", false);
        response.put("message", "坐标解析失败");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String region) {
        Map<String, Object> response = new HashMap<>();
        String searchRegion = (region != null && !region.isEmpty()) ? region : "全国";
        try {
            JsonNode result = baiduMapService.searchPlace(keyword, searchRegion);
            if (result != null && result.has("status")) {
                int status = result.get("status").asInt();
                if (status == 0) {
                    List<Map<String, Object>> dataList = new ArrayList<>();
                    if (result.has("result") && result.get("result").isArray()) {
                        for (JsonNode item : result.get("result")) {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("name", item.has("name") ? item.get("name").asText() : "");
                            itemMap.put("address", item.has("address") ? item.get("address").asText() : "");
                            if (item.has("location") && item.get("location").has("lat")) {
                                itemMap.put("latitude", item.get("location").get("lat").asDouble());
                                itemMap.put("longitude", item.get("location").get("lng").asDouble());
                            }
                            dataList.add(itemMap);
                        }
                    }
                    response.put("success", true);
                    response.put("data", dataList);
                    response.put("count", dataList.size());
                    return ResponseEntity.ok(response);
                } else {
                    String message = result.has("message") ? result.get("message").asText() : "搜索失败";
                    response.put("success", false);
                    response.put("message", message);
                    return ResponseEntity.badRequest().body(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.put("success", false);
        response.put("message", "搜索失败");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/default-location")
    public ResponseEntity<Map<String, Object>> getDefaultLocation() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("latitude", 39.9042);
        response.put("longitude", 116.4074);
        response.put("address", "北京市东城区天安门广场");
        return ResponseEntity.ok(response);
    }
}