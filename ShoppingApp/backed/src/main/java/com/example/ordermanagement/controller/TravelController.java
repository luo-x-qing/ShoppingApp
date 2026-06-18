package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.SiliconService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    private SiliconService siliconService;

    @Autowired
    private AmapService amapService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double CLOSE_THRESHOLD_KM = 5.0;

    @GetMapping("/plan")
    public ResponseEntity<Map<String, Object>> plan(
            @RequestParam String city,
            @RequestParam(defaultValue = "2") int days,
            @RequestParam(defaultValue = "0") int budget,
            @RequestParam(defaultValue = "1") int travelers,
            @RequestParam(required = false) String spots) {

        List<SpotInfo> spotInfos = geocodeSpots(spots, city);
        String prompt = buildPrompt(city, days, budget, travelers, spots, spotInfos);

        String aiReply = siliconService.chat(prompt);
        if (aiReply.isEmpty()) {
            return ResponseEntity.badRequest().body(error("AI暂无回复，请重试"));
        }

        Map<String, Object> result = parseJsonResult(aiReply, city, days, budget, travelers, spotInfos);
        if (result == null) {
            return ResponseEntity.badRequest().body(error("AI返回格式异常，请重试"));
        }

        ensureAllSpotsOnMap(result, spotInfos);

        return ResponseEntity.ok(result);
    }

    private List<SpotInfo> geocodeSpots(String spots, String city) {
        List<SpotInfo> list = new ArrayList<>();
        if (spots == null || spots.trim().isEmpty()) return list;
        for (String name : spots.split("\\s*[,，]\\s*")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            String loc = amapService.geo(trimmed, city);
            if (loc != null) {
                String[] parts = loc.split(",");
                try {
                    list.add(new SpotInfo(trimmed, Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
                } catch (NumberFormatException e) {
                    list.add(new SpotInfo(trimmed, null, null));
                }
            } else {
                list.add(new SpotInfo(trimmed, null, null));
            }
        }
        return list;
    }

    private String buildPrompt(String city, int days, int budget, int travelers,
                               String spots, List<SpotInfo> spotInfos) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是专业的旅游线路规划师。请根据以下信息规划详细的每日行程。\n\n");
        sb.append("目的地城市：").append(city).append("\n");
        sb.append("行程天数：").append(days).append("天\n");
        sb.append("预算金额：").append(budget > 0 ? budget + "元" : "不限").append("\n");
        sb.append("出行人数：").append(travelers).append("人\n");
        if (spots != null && !spots.isEmpty()) {
            sb.append("用户已选景点：").append(spots).append("\n");
        }

        String spatialCtx = buildSpatialContext(spotInfos);
        if (!spatialCtx.isEmpty()) {
            sb.append("\n").append(spatialCtx);
        }

        sb.append("\n");
        sb.append("请严格按照以下JSON格式回复，不要包含任何多余文字或markdown标记：\n");
        sb.append("{\n");
        sb.append("  \"days\": [\n");
        sb.append("    {\n");
        sb.append("      \"day\": 1,\n");
        sb.append("      \"schedule\": [\n");
        sb.append("        { \"time\": \"08:00\", \"type\": \"breakfast\", \"content\": \"内容描述\", \"location\": \"餐厅名\" },\n");
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
        sb.append("2. scenic 类型的条目必须包含 spotName 字段，值为用户提供的原始景点全名，不得修改或省略\n");
        sb.append("3. scenic 条目的 content 字段必须以「游览+景点名」开头（如「游览大雁塔：参观南广场、北广场音乐喷泉」），让用户一眼知道在哪个景点\n");
        sb.append("4. 每天的行程应包含早餐、午餐、晚餐、景点游览\n");
        sb.append("5. 景点之间应安排合理的交通方式（transport类型），写具体方式和时间\n");
        sb.append("6. 考虑预算和人数，合理安排餐饮和交通\n");
        sb.append("7. 用户已选景点必须全部安排进行程，spotName 必须与用户提供的景点名完全一致\n");
        sb.append("8. 行程安排必须遵循地理距离原则：\n");
        sb.append("   a) 间距5km以内的景点安排在同一天游览\n");
        sb.append("   b) 间距超过5km的景点必须安排在不同天\n");
        sb.append("   c) 如果某个景点远离其他所有景点，为该景点单独安排一天，并在当天加入该景点附近的游玩项目（周边小景点、美食街、商圈等）\n");
        sb.append("   d) 每天安排1-3个景点，不要过于紧凑\n");
        sb.append("9. 所有餐饮（早餐、午餐、晚餐）必须写具体餐厅名称（如「海底捞（大雁塔店）」「老孙家羊肉泡馍」），不能写「当地餐厅」等模糊描述。餐厅应在当日游览的景点附近\n");
        sb.append("10. 住宿（hotel类型）推荐景点附近的真实酒店名称\n");
        sb.append("11. 如果用户选择景点较少而天数较多，多余天数可安排周边游或自由活动\n");
        sb.append("12. 请直接输出纯JSON，不要输出任何解释文字");

        return sb.toString();
    }

    private String buildSpatialContext(List<SpotInfo> spotInfos) {
        List<SpotInfo> valid = spotInfos.stream()
                .filter(s -> s.lng != null && s.lat != null)
                .collect(Collectors.toList());
        if (valid.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("【用户所选景点的地理分布分析】\n");

        for (SpotInfo s : valid) {
            List<String> dists = new ArrayList<>();
            for (SpotInfo o : valid) {
                if (o == s) continue;
                double d = haversineKm(s.lat, s.lng, o.lat, o.lng);
                dists.add(o.name + "约" + String.format("%.1f", d) + "km");
            }
            sb.append("- ").append(s.name).append(" 距 ");
            sb.append(String.join("，", dists)).append("\n");
        }

        List<List<SpotInfo>> clusters = clusterSpots(valid);
        sb.append("\n聚类建议：\n");
        for (int i = 0; i < clusters.size(); i++) {
            List<SpotInfo> group = clusters.get(i);
            String names = group.stream().map(s -> s.name).collect(Collectors.joining("、"));
            if (group.size() == 1) {
                double minDist = Double.MAX_VALUE;
                for (int k = 0; k < clusters.size(); k++) {
                    if (k == i) continue;
                    for (SpotInfo o : clusters.get(k)) {
                        double d = haversineKm(group.get(0).lat, group.get(0).lng, o.lat, o.lng);
                        if (d < minDist) minDist = d;
                    }
                }
                sb.append("- ").append(names).append("（距最近景点约")
                        .append(String.format("%.1f", minDist)).append("km，建议单独安排一天及周边游玩）\n");
            } else {
                sb.append("- ").append(names).append("（间距较小，建议同一天游览）\n");
            }
        }

        sb.append("请严格按照上述距离分析安排每日景点，不得将远距离景点安排在同一天。");
        return sb.toString();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<List<SpotInfo>> clusterSpots(List<SpotInfo> spots) {
        int n = spots.size();
        boolean[] assigned = new boolean[n];
        List<List<SpotInfo>> groups = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (assigned[i]) continue;
            List<SpotInfo> group = new ArrayList<>();
            group.add(spots.get(i));
            assigned[i] = true;
            for (int j = i + 1; j < n; j++) {
                if (assigned[j]) continue;
                double d = haversineKm(spots.get(i).lat, spots.get(i).lng,
                        spots.get(j).lat, spots.get(j).lng);
                if (d <= CLOSE_THRESHOLD_KM) {
                    group.add(spots.get(j));
                    assigned[j] = true;
                }
            }
            groups.add(group);
        }
        return groups;
    }

    @SuppressWarnings("unchecked")
    private void ensureAllSpotsOnMap(Map<String, Object> result, List<SpotInfo> originalSpots) {
        if (originalSpots == null || originalSpots.isEmpty()) return;
        Object raw = result.get("scenicSpots");
        if (!(raw instanceof List)) return;
        List<Map<String, Object>> scenicSpots = (List<Map<String, Object>>) raw;

        Set<String> present = scenicSpots.stream()
                .map(s -> (String) s.get("name"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int maxOrder = scenicSpots.stream()
                .mapToInt(s -> (int) s.getOrDefault("order", 0))
                .max().orElse(0);

        for (SpotInfo orig : originalSpots) {
            if (orig.lng == null || orig.lat == null) continue;
            boolean matched = present.stream().anyMatch(n ->
                    n.equals(orig.name) || n.contains(orig.name) || orig.name.contains(n));
            if (!matched) {
                maxOrder++;
                Map<String, Object> spot = new LinkedHashMap<>();
                spot.put("order", maxOrder);
                spot.put("name", orig.name);
                spot.put("day", 1);
                spot.put("lng", orig.lng);
                spot.put("lat", orig.lat);
                scenicSpots.add(spot);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResult(String aiReply, String city, int days,
                                                 int budget, int travelers, List<SpotInfo> originalSpots) {
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
                            String locStr = amapService.geo(spotName, city);
                            if (locStr != null) {
                                String[] parts = locStr.split(",");
                                spot.put("lng", Double.parseDouble(parts[0]));
                                spot.put("lat", Double.parseDouble(parts[1]));
                            } else if (location != null && !location.isEmpty()) {
                                String locStr2 = amapService.geo(location, city);
                                if (locStr2 != null) {
                                    String[] parts = locStr2.split(",");
                                    spot.put("lng", Double.parseDouble(parts[0]));
                                    spot.put("lat", Double.parseDouble(parts[1]));
                                }
                            }

                            // fallback: match against user's original spots
                            if (spot.get("lng") == null && originalSpots != null) {
                                for (SpotInfo orig : originalSpots) {
                                    if (orig.lng == null || orig.lat == null) continue;
                                    if (orig.name.equals(spotName)
                                            || orig.name.contains(spotName)
                                            || spotName.contains(orig.name)) {
                                        spot.put("lng", orig.lng);
                                        spot.put("lat", orig.lat);
                                        break;
                                    }
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

    private static class SpotInfo {
        String name;
        Double lng;
        Double lat;
        SpotInfo(String name, Double lng, Double lat) {
            this.name = name;
            this.lng = lng;
            this.lat = lat;
        }
    }
}
