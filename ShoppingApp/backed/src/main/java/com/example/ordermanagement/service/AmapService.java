package com.example.ordermanagement.service;

import org.springframework.beans.factory.annotation.Value;
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

    public String geo(String address) {
        try {
            String addr = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address="
                    + addr + "&key=" + amapKey;
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                List<Map> geocodes = (List<Map>) response.get("geocodes");
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

            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                Map<?, ?> route = (Map<?, ?>) ((List<?>) response.get("route")).get(0);
                List<Map> paths = (List<Map>) route.get("paths");
                if (paths != null && !paths.isEmpty()) {
                    Map path = paths.get(0);
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

    public Map<String, String> geoDetail(String address) {
        Map<String, String> detail = new HashMap<>();
        try {
            String addr = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/geocode/geo?address="
                    + addr + "&key=" + amapKey;
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "1".equals(response.get("status"))) {
                List<Map> geocodes = (List<Map>) response.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    Map geo = geocodes.get(0);
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
