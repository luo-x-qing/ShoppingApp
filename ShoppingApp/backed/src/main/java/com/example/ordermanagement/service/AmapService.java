package com.example.ordermanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AmapService {

    @Value("${amap.web.key}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String geo(String address) {
        try {
            String addr = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address="
                    + addr + "&key=" + amapKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) response.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    return (String) geocodes.get(0).get("location");
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public String geocode(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey;
            System.out.println("[地理编码] 请求: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            System.out.println("[地理编码] 响应: " + response);

            if (response != null && "1".equals(response.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) response.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    return (String) geocodes.get(0).get("location");
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("[地理编码] 失败: " + address + ", 错误: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public String geocodeWithRetry(String address, int maxRetries) {
        int retryCount = 0;
        long waitTime = 3000;
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        while (retryCount <= maxRetries) {
            if (retryCount > 0) {
                System.out.println("[地理编码] 第" + retryCount + "次重试: " + address + ", 等待" + waitTime + "ms");
                try { Thread.sleep(waitTime); } catch (InterruptedException e) { }
                waitTime *= 2;
            }
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
                String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey;
                System.out.println("[地理编码] 请求: " + url);

                ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map<String, Object> response = responseEntity.getBody();
                System.out.println("[地理编码] 响应: " + response);

                if (response != null && "1".equals(response.get("status"))) {
                    List<Map<String, Object>> geocodes = (List<Map<String, Object>>) response.get("geocodes");
                    if (geocodes != null && !geocodes.isEmpty()) {
                        return (String) geocodes.get(0).get("location");
                    }
                }
                if (response != null && "0".equals(response.get("status"))) {
                    String info = (String) response.getOrDefault("info", "");
                    if (info.contains("CUQPS_HAS_EXCEEDED_THE_LIMIT") || info.contains("ENGINE_RESPONSE_DATA_ERROR")) {
                        retryCount++;
                        continue;
                    }
                }
                return null;
            } catch (Exception e) {
                System.err.println("[地理编码] 失败: " + address + ", 错误: " + e.getMessage());
                retryCount++;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> geocodeDetail(String address) {
        Map<String, String> detail = new HashMap<>();
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey;
            System.out.println("[地理编码] 请求: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            System.out.println("[地理编码] 响应: " + response);

            if (response != null && "1".equals(response.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) response.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    Map<String, Object> geo = geocodes.get(0);
                    detail.put("province", (String) geo.getOrDefault("province", ""));
                    Object cityObj = geo.get("city");
                    detail.put("city", cityObj instanceof String && !((String) cityObj).isEmpty() ? (String) cityObj : "");
                    detail.put("district", (String) geo.getOrDefault("district", ""));
                    String location = (String) geo.get("location");
                    detail.put("location", location != null && !location.isEmpty() ? location : "");
                    detail.put("formattedAddress", (String) geo.getOrDefault("formatted_address", ""));
                }
            } else if (response != null) {
                String info = (String) response.get("info");
                detail.put("error", info);
                System.err.println("[地理编码] 请求失败, status=" + response.get("status") + ", info=" + info);
            }
        } catch (Exception e) {
            e.printStackTrace();
            detail.put("error", e.getMessage());
        }
        return detail;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> drivingRoute(String[] spotNames) {
        Map<String, Object> result = new HashMap<>();
        try {
            String origin = geo(spotNames[0]);
            String destination = geo(spotNames[spotNames.length - 1]);
            if (origin == null || destination == null) {
                return result;
            }

            StringBuilder waypoints = new StringBuilder();
            for (int i = 1; i < spotNames.length - 1; i++) {
                String loc = geo(spotNames[i]);
                if (loc != null) {
                    if (waypoints.length() > 0) waypoints.append("|");
                    waypoints.append(loc);
                }
            }

            String url = "https://restapi.amap.com/v3/direction/driving"
                    + "?origin=" + origin
                    + "&destination=" + destination
                    + "&key=" + amapKey;
            if (waypoints.length() > 0) {
                url += "&waypoints=" + waypoints;
            }

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                Map<String, Object> route = (Map<String, Object>) ((List<?>) response.get("route")).get(0);
                List<Map<String, Object>> paths = (List<Map<String, Object>>) route.get("paths");
                if (paths != null && !paths.isEmpty()) {
                    Map<String, Object> path = paths.get(0);
                    result.put("distance", path.get("distance"));
                    result.put("duration", path.get("duration"));
                    result.put("polyline", path.get("polyline"));
                    result.put("tolls", path.get("tolls"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Object> geos(String[] addresses) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> locations = new ArrayList<>();
        for (String addr : addresses) {
            String loc = geo(addr.trim());
            Map<String, String> item = new HashMap<>();
            item.put("name", addr.trim());
            item.put("location", loc != null ? loc : "");
            locations.add(item);
        }
        result.put("locations", locations);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> geoDetail(String address) {
        Map<String, String> detail = new HashMap<>();
        try {
            String addr = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address="
                    + addr + "&key=" + amapKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) response.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    Map<String, Object> geo = geocodes.get(0);
                    detail.put("province", (String) geo.getOrDefault("province", ""));
                    Object cityObj = geo.get("city");
                    if (cityObj instanceof String && !((String) cityObj).isEmpty()) {
                        detail.put("city", (String) cityObj);
                    } else {
                        detail.put("city", (String) geo.getOrDefault("province", ""));
                    }
                    detail.put("district", (String) geo.getOrDefault("district", ""));
                    detail.put("location", (String) geo.getOrDefault("location", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }
}
