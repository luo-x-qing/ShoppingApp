package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.SiliconService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    private SiliconService siliconService;

    @Autowired
    private AmapService amapService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/plan")
    public ResponseEntity<Map<String, Object>> plan(
            @RequestParam String city,
            @RequestParam(defaultValue = "2") int days,
            @RequestParam(defaultValue = "0") int budget,
            @RequestParam(defaultValue = "1") int travelers,
            @RequestParam(required = false) String spots) {

        String prompt = buildPrompt(city, days, budget, travelers, spots);

        String aiReply = siliconService.chat(prompt);
        if (aiReply.isEmpty()) {
            return ResponseEntity.badRequest().body(error("AI暂无回复，请重试"));
        }

        Map<String, Object> result = parseJsonResult(aiReply, city, days, budget, travelers);
        if (result == null) {
            return ResponseEntity.badRequest().body(error("AI返回格式异常，请重试"));
        }

        return ResponseEntity.ok(result);
    }

    private String buildPrompt(String city, int days, int budget, int travelers, String spots) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是专业的旅游线路规划师。请根据以下信息规划详细的每日行程。\n\n");
        sb.append("目的地城市：").append(city).append("\n");
        sb.append("行程天数：").append(days).append("天\n");
        sb.append("预算金额：").append(budget > 0 ? budget + "元" : "不限").append("\n");
        sb.append("出行人数：").append(travelers).append("人\n");
        if (spots != null && !spots.isEmpty()) {
            sb.append("用户已选景点：").append(spots).append("\n");
        }
        sb.append("\n");
        sb.append("请严格按照以下JSON格式回复，不要包含任何多余文字或markdown标记：\n");
        sb.append("{\n");
        sb.append("  \"days\": [\n");
        sb.append("    {\n");
        sb.append("      \"day\": 1,\n");
        sb.append("      \"schedule\": [\n");
        sb.append("        { \"time\": \"08:00\", \"type\": \"breakfast\", \"content\": \"内容描述\", \"location\": \"地点\" },\n");
        sb.append("        { \"time\": \"09:00-11:30\", \"type\": \"scenic\", \"content\": \"游览内容\", \"location\": \"地点\", \"spotName\": \"景点名\" },\n");
        sb.append("        { \"time\": \"11:30-12:30\", \"type\": \"lunch\", \"content\": \"午餐内容\", \"location\": \"餐厅名\" },\n");
        sb.append("        { \"time\": \"14:00-17:00\", \"type\": \"scenic\", \"content\": \"游览内容\", \"location\": \"地点\", \"spotName\": \"景点名\" },\n");
        sb.append("        { \"time\": \"18:00-19:00\", \"type\": \"dinner\", \"content\": \"晚餐内容\", \"location\": \"餐厅名\" }\n");
        sb.append("      ]\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"totalBudget\": 预估总花费（数字）,\n");
        sb.append("  \"tips\": \"温馨提示语\"\n");
        sb.append("}\n\n");
        sb.append("要求：\n");
        sb.append("1. type 只能是以下之一：breakfast, lunch, dinner, scenic, transport, hotel, free\n");
        sb.append("2. scenic 类型的条目必须包含 spotName 字段（景点全名）\n");
        sb.append("3. 每天的行程应包含早餐、午餐、晚餐、景点游览\n");
        sb.append("4. 景点之间应安排合理的交通方式（transport类型）\n");
        sb.append("5. 考虑预算和人数，合理安排餐饮和交通\n");
        sb.append("6. 如果用户提供了已选景点，优先安排这些景点\n");
        sb.append("7. 每天安排2-4个景点，不要过于紧凑");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResult(String aiReply, String city, int days, int budget, int travelers) {
        try {
            String json = aiReply.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("(?s)```(?:json)?\\s*", "").trim();
            }

            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);

            List<Map<String, Object>> daysList = (List<Map<String, Object>>) parsed.get("days");
            if (daysList == null || daysList.isEmpty()) return null;

            List<Map<String, Object>> allScenicSpots = new ArrayList<>();
            int globalOrder = 0;

            for (Map<String, Object> dayObj : daysList) {
                List<Map<String, Object>> schedule = (List<Map<String, Object>>) dayObj.get("schedule");
                if (schedule == null) continue;

                for (Map<String, Object> item : schedule) {
                    if ("scenic".equals(item.get("type"))) {
                        String spotName = (String) item.get("spotName");
                        if (spotName != null && !spotName.isEmpty()) {
                            globalOrder++;
                            Map<String, Object> spot = new LinkedHashMap<>();
                            spot.put("order", globalOrder);
                            spot.put("name", spotName);
                            spot.put("day", dayObj.get("day"));

                            String location = (String) item.get("location");
                            String locStr = amapService.geo(spotName);
                            if (locStr != null) {
                                String[] parts = locStr.split(",");
                                spot.put("lng", Double.parseDouble(parts[0]));
                                spot.put("lat", Double.parseDouble(parts[1]));
                            } else if (location != null && !location.isEmpty()) {
                                String locStr2 = amapService.geo(location);
                                if (locStr2 != null) {
                                    String[] parts = locStr2.split(",");
                                    spot.put("lng", Double.parseDouble(parts[0]));
                                    spot.put("lat", Double.parseDouble(parts[1]));
                                }
                            }
                            allScenicSpots.add(spot);
                        }
                    }
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("city", city);
            result.put("days", days);
            result.put("budget", budget);
            result.put("travelers", travelers);
            result.put("itinerary", daysList);
            result.put("scenicSpots", allScenicSpots);
            result.put("totalBudget", parsed.getOrDefault("totalBudget", 0));
            result.put("tips", parsed.getOrDefault("tips", ""));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", msg);
        return m;
    }
}
