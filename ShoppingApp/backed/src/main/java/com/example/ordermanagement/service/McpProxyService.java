package com.example.ordermanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class McpProxyService {

    private static final String MCP_PROXY_URL = "http://localhost:3000/mcp";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String searchFlights(String dep, String arr, String date) {
        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("tool", "searchFlightsByDepArr");

            Map<String, String> args = new LinkedHashMap<>();
            args.put("dep", dep);
            args.put("arr", arr);
            args.put("date", date);
            requestBody.put("arguments", args);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                MCP_PROXY_URL, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("data") && root.get("data").has("content")) {
                JsonNode content = root.get("data").get("content");
                for (JsonNode item : content) {
                    if (item.has("text")) {
                        String text = item.get("text").asText();
                        return extractFlightsFromText(text);
                    }
                }
            }
            return response.getBody();
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String extractFlightsFromText(String text) {
        return text;
    }
}
