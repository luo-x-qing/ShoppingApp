package com.example.ordermanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SiliconService {

    @Value("${ai.silicon.url}")
    private String url;

    @Value("${ai.silicon.key}")
    private String key;

    @Value("${ai.silicon.model}")
    private String model;

    private final RestTemplate restTemplate;

    public SiliconService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);
        restTemplate = new RestTemplate(factory);
    }

    public String chat(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", Collections.singletonList(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map choice = choices.get(0);
                Map msg = (Map) choice.get("message");
                return (String) msg.get("content");
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
