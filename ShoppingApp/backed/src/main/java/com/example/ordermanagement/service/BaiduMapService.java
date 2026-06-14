package com.example.ordermanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BaiduMapService {

    @Value("${baidu.map.api.key}")
    private String ak;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode geocode(String address) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.map.baidu.com/geocoding/v3/")
                .queryParam("address", address)
                .queryParam("output", "json")
                .queryParam("ak", ak)
                .encode()
                .toUriString();
        try {
            System.out.println("调用百度地图地理编码API: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("返回结果: " + response);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("地理编码失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode reverseGeocode(double lat, double lng) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.map.baidu.com/reverse_geocoding/v3/")
                .queryParam("location", lat + "," + lng)
                .queryParam("output", "json")
                .queryParam("pois", 1)
                .queryParam("ak", ak)
                .encode()
                .toUriString();
        try {
            System.out.println("调用百度地图逆地理编码API: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("返回结果: " + response);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("逆地理编码失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode searchPlace(String query, String region) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.map.baidu.com/place/v2/suggestion")
                .queryParam("query", query)
                .queryParam("region", region)
                .queryParam("output", "json")
                .queryParam("ak", ak)
                .encode()
                .toUriString();
        try {
            System.out.println("调用百度地图地点搜索API: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("返回结果: " + response);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("地点搜索失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode searchNearbyPoi(String query, String location) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.map.baidu.com/place/v2/search")
                .queryParam("query", query)
                .queryParam("location", location)
                .queryParam("radius", 2000)
                .queryParam("output", "json")
                .queryParam("ak", ak)
                .encode()
                .toUriString();
        try {
            System.out.println("调用百度地图周边检索API: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("返回结果: " + response);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("周边检索失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}