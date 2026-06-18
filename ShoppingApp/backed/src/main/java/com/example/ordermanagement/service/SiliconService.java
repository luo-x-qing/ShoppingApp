package com.example.ordermanagement.service;

import com.example.ordermanagement.model.dto.AIMessage;
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
        factory.setReadTimeout(60000);
        restTemplate = new RestTemplate(factory);
    }

    // 原有方法：单次对话
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

    // 新增方法：多轮对话（供前端调用）
    public String chatWithMessages(List<AIMessage> messages) {
        List<Map<String, String>> fullMessages = buildFullMessages(messages);
        return callSiliconApi(fullMessages);
    }

    // 构造带系统提示词的完整消息列表
    private List<Map<String, String>> buildFullMessages(List<AIMessage> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        // 系统提示词
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是专业的旅游规划助手。用户会提供出发地、目的地、日期等信息，你需要给出详细的行程建议，包括交通方式、景点推荐、住宿建议、预算参考等。回复要清晰、实用、分点说明，使用中文，适当加emoji。");
        result.add(systemMsg);
        // 历史消息
        for (AIMessage msg : messages) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            result.add(m);
        }
        return result;
    }

    // 私有方法：实际调用硅基 API
    private String callSiliconApi(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(key);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

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
            return "抱歉，AI 服务返回异常。";
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 服务调用失败：" + e.getMessage();
        }
    }
}