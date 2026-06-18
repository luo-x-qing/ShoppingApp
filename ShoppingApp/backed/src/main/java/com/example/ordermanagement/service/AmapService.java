package com.example.ordermanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AmapService {

    @Value("${amap.web.key}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> httpGetJson(String urlStr) throws Exception {
        String body = httpGet(urlStr);
        return objectMapper.readValue(body, Map.class);
    }

    @SuppressWarnings("unchecked")
    public String geo(String address) {
        try {
            String addr = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address="
                    + addr + "&key=" + amapKey + "&output=JSON";
            Map<String, Object> response = httpGetJson(url);
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
            String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey + "&output=JSON";
            System.out.println("[地理编码] 请求: " + url);

            Map<String, Object> response = httpGetJson(url);
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
        while (retryCount <= maxRetries) {
            if (retryCount > 0) {
                System.out.println("[地理编码] 第" + retryCount + "次重试: " + address + ", 等待" + waitTime + "ms");
                try { Thread.sleep(waitTime); } catch (InterruptedException e) { }
                waitTime *= 2;
            }
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
                String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey + "&output=JSON";
                System.out.println("[地理编码] 请求: " + url);

                Map<String, Object> response = httpGetJson(url);
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

    public String testRawGeocode(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String urlStr = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey + "&output=JSON";
            System.out.println("[原始请求] " + urlStr);

            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int code = conn.getResponseCode();
            System.out.println("[原始响应] HTTP " + code);

            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();
            String body = sb.toString();
            System.out.println("[原始响应] Body: " + body);
            return body;
        } catch (Exception e) {
            System.err.println("[原始请求] 异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> geocodeDetail(String address) {
        Map<String, String> detail = new HashMap<>();
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address=" + encodedAddress + "&key=" + amapKey + "&output=JSON";
            System.out.println("[地理编码] 请求: " + url);

            Map<String, Object> response = httpGetJson(url);
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
