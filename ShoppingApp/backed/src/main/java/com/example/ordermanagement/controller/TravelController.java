package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.SiliconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    private SiliconService siliconService;

    @Autowired
    private AmapService amapService;

    @GetMapping("/plan")
    public Map<String, Object> plan(
            @RequestParam String city,
            @RequestParam(defaultValue = "2") int days) {

        String prompt = String.format(
                "你是旅游规划师。用户想去%s游玩%d天。\n" +
                "请推荐最合适的景点，按游玩顺序列出，只输出景点名称，用逗号分隔，不要多余文字。",
                city, days);

        String spotStr = siliconService.chat(prompt);
        String[] spots = spotStr.split("[，,、]");
        List<String> spotList = new ArrayList<>();
        for (String s : spots) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                spotList.add(trimmed);
            }
        }

        String[] spotArray = spotList.toArray(new String[0]);
        Map<String, Object> routeResult = amapService.drivingRoute(spotArray);
        Map<String, Object> geoResult = amapService.geos(spotArray);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city", city);
        result.put("days", days);
        result.put("spots", spotList);
        result.put("route", routeResult);
        result.put("locations", geoResult.get("locations"));
        return result;
    }
}
