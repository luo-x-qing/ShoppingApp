package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.dto.AIMessage;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private HotelService hotelService;

    public SiliconService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(180000);
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
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");

        // 获取数据库酒店列表
        String hotelList = getHotelListForPrompt();

        systemMsg.put("content",
                "你是专业的旅游规划助手。\n\n" +
                        "【核心任务】\n" +
                        "用户会提供出发地、目的地、日期等信息，你需要给出详细的行程建议，包括交通方式、景点推荐、住宿建议、预算参考等。\n\n" +
                        "【航班推荐格式 - 必须严格遵守】\n" +
                        "当行程涉及城市间交通时，你必须在行程描述中插入航班推荐标记，格式为：\n" +
                        "[FLIGHT:出发城市|到达城市|日期|预估价格(可选)]\n\n" +
                        "示例：\n" +
                        "- [FLIGHT:北京|杭州|2026-07-01|720]\n" +
                        "- [FLIGHT:杭州|北京|2026-07-05|680]\n\n" +
                        "【酒店推荐格式 - 必须严格遵守】\n" +
                        "当行程需要住宿时，你必须在行程描述中插入酒店推荐标记，格式为：\n" +
                        "[HOTEL:酒店名称|城市|每晚价格|酒店星级]\n\n" +
                        "【可推荐的酒店列表 - 必须从中选择】\n" +
                        "以下是数据库中实际存在的酒店，你只能从中推荐：\n" +
                        hotelList + "\n\n" +
                        "【规则】\n" +
                        "1. 航班推荐应紧跟在对应的交通描述后，酒店推荐紧跟在对应的住宿描述后。\n" +
                        "2. 日期格式必须为 YYYY-MM-DD。\n" +
                        "3. 城市名称使用中文。\n" +
                        "4. 预估价格可选，如果知道就填，不知道可以省略（但保留竖线占位）。\n" +
                        "5. 回复要清晰、实用、分点说明，使用中文，适当加emoji。\n" +
                        "6. 如果没有航班或酒店需求，则不需要添加对应标记。\n" +
                        "7. 酒店名称必须与可推荐酒店列表中的名称完全一致！" +
                        "8. 用户说\"福州酒店\" → 只推荐福州的酒店" +
                        "9. 用户说\"浙江酒店\" → 只推荐浙江的酒店" +
                        "10. 用户输入的日期（如 7.4、7月4日、2026-07-04）必须被正确解析为 2026-07-04 格式。\n" +
                        "11. 航班推荐中的日期必须与用户指定的出发和返回日期完全一致。\n" +
                        "12. 如果用户说 '7.4从北京去福州'，出发日期必须是 7月4日，返回日期必须是 7月7日或之后。\n" +
                        "13. 绝对不要使用其他日期，更不要使用 5月、6月等错误月份。\n\n" +
                        "【日期格式】\n" +
                        "- 用户说 '7.4' → 表示 7月4日（同一年）\n" +
                        "- 用户说 '7月4日' → 表示 7月4日\n" +
                        "- 用户说 '2026-07-04' → 表示 2026年7月4日\n" +
                        "- 所有日期统一使用 YYYY-MM-DD 格式\n\n" +
                        "【示例】\n" +
                        "用户：7.4从北京去福州玩4天\n" +
                        "你应返回：\n" +
                        "[FLIGHT:北京|福州|2026-07-04|720]\n" +
                        "[FLIGHT:福州|北京|2026-07-08|680]\n\n" +
                        "【重要】日期必须与用户输入一致，不能自己编造！"
        );
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

    // 获取酒店列表供 AI 参考
    private String getHotelListForPrompt() {
        try {
            // 获取所有酒店（包括 yunduo 酒店）
            List<Hotel> hotels = hotelService.getAllHotels();
            if (hotels == null || hotels.isEmpty()) {
                return "- 暂无酒店数据\n";
            }
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Hotel hotel : hotels) {
                if (count >= 80) break; // 增加到 80 个
                // 从 address 提取城市
                String city = "未知城市";
                if (hotel.getAddress() != null && !hotel.getAddress().isEmpty()) {
                    String addr = hotel.getAddress();
                    // 提取城市（省市区中的市）
                    if (addr.contains("市")) {
                        int idx = addr.indexOf("市");
                        if (idx > 0 && idx <= 6) {
                            city = addr.substring(0, idx + 1);
                        } else {
                            city = addr.substring(0, Math.min(addr.length(), 4));
                        }
                    } else {
                        city = addr.substring(0, Math.min(addr.length(), 4));
                    }
                }
                // 获取价格和星级
                Integer starLevel = hotel.getStarLevel() != null ? hotel.getStarLevel() : 3;
                Double price = hotel.getPrice() != null ? hotel.getPrice() : 300.0;

                sb.append("- ").append(hotel.getName())
                        .append("（").append(city)
                        .append("，").append(starLevel)
                        .append("星级，约¥").append(price.intValue())
                        .append("/晚）\n");
                count++;
            }
            System.out.println("提供给AI的酒店列表数量：" + count);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "- 暂无酒店数据\n";
        }
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