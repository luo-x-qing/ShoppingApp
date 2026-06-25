package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.SiliconService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SiliconService siliconService;

    @Mock
    private AmapService amapService;

    @InjectMocks
    private TravelController travelController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(travelController).build();
    }

    // ==================== Public Plan Endpoint Tests ====================

    @Test
    void testPlanSuccess() throws Exception {
        when(amapService.geo(eq("宽窄巷子"), eq("成都市"))).thenReturn("104.06,30.67");
        when(siliconService.chat(anyString())).thenReturn(
                "{" +
                "  \"days\": [{" +
                "    \"day\": 1," +
                "    \"schedule\": [" +
                "      { \"time\": \"08:00\", \"type\": \"breakfast\", \"content\": \"早餐\", \"location\": \"餐厅\" }," +
                "      { \"time\": \"09:00-11:30\", \"type\": \"scenic\", \"content\": \"游览宽窄巷子\", \"location\": \"宽窄巷子\", \"spotName\": \"宽窄巷子\" }" +
                "    ]" +
                "  }]," +
                "  \"totalBudget\": 500," +
                "  \"tips\": \"注意防晒\"" +
                "}"
        );

        mockMvc.perform(get("/travel/plan")
                        .param("city", "成都市")
                        .param("days", "2")
                        .param("budget", "2000")
                        .param("travelers", "2")
                        .param("spots", "宽窄巷子"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("成都市")))
                .andExpect(jsonPath("$.days", is(2)))
                .andExpect(jsonPath("$.budget", is(2000)))
                .andExpect(jsonPath("$.travelers", is(2)))
                .andExpect(jsonPath("$.itinerary", notNullValue()))
                .andExpect(jsonPath("$.scenicSpots", notNullValue()))
                .andExpect(jsonPath("$.totalBudget", is(500)))
                .andExpect(jsonPath("$.tips", is("注意防晒")));
    }

    @Test
    void testPlanAiNoReply() throws Exception {
        when(amapService.geo(anyString(), anyString())).thenReturn("104.06,30.67");
        when(siliconService.chat(anyString())).thenReturn("");

        mockMvc.perform(get("/travel/plan")
                        .param("city", "成都市")
                        .param("spots", "宽窄巷子"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("AI暂无回复")));
    }

    @Test
    void testPlanInvalidJson() throws Exception {
        when(amapService.geo(anyString(), anyString())).thenReturn("104.06,30.67");
        when(siliconService.chat(anyString())).thenReturn("这不是JSON");

        mockMvc.perform(get("/travel/plan")
                        .param("city", "成都市")
                        .param("spots", "宽窄巷子"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("格式异常")));
    }

    @Test
    void testPlanWithJsonCodeFence() throws Exception {
        when(amapService.geo(anyString(), anyString())).thenReturn("104.06,30.67");
        when(siliconService.chat(anyString())).thenReturn(
                "```json\n" +
                "{" +
                "  \"days\": [{" +
                "    \"day\": 1," +
                "    \"schedule\": [" +
                "      { \"time\": \"09:00\", \"type\": \"scenic\", \"content\": \"游览景点\", \"location\": \"某地\", \"spotName\": \"宽窄巷子\" }" +
                "    ]" +
                "  }]," +
                "  \"totalBudget\": 300," +
                "  \"tips\": \"tip\"" +
                "}" +
                "\n```"
        );

        mockMvc.perform(get("/travel/plan")
                        .param("city", "成都市")
                        .param("spots", "宽窄巷子"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tips", is("tip")));
    }

    @Test
    void testPlanWithNoSpots() throws Exception {
        when(siliconService.chat(anyString())).thenReturn(
                "{" +
                "  \"days\": [{" +
                "    \"day\": 1," +
                "    \"schedule\": [" +
                "      { \"time\": \"08:00\", \"type\": \"breakfast\", \"content\": \"早餐\", \"location\": \"餐厅\" }" +
                "    ]" +
                "  }]," +
                "  \"totalBudget\": 100," +
                "  \"tips\": \"\"" +
                "}"
        );

        mockMvc.perform(get("/travel/plan")
                        .param("city", "成都市")
                        .param("days", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenicSpots", hasSize(0)));
    }

    // ==================== Spatial Algorithm Tests (via reflection) ====================

    @Test
    void testHaversineKm() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("haversineKm", double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        double result = (double) method.invoke(travelController, 30.57, 104.06, 30.67, 104.07);
        assertTrue(result > 0);
        assertTrue(result < 20);

        double samePoint = (double) method.invoke(travelController, 30.57, 104.06, 30.57, 104.06);
        assertEquals(0.0, samePoint, 0.001);

        double farApart = (double) method.invoke(travelController, 30.57, 104.06, 39.90, 116.40);
        assertTrue(farApart > 1000);
    }

    @Test
    void testClusterSpots() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("clusterSpots", List.class);
        method.setAccessible(true);

        List<Object> spots = new ArrayList<>();
        spots.add(createSpotInfo("武侯祠", 104.05, 30.65));
        spots.add(createSpotInfo("锦里", 104.06, 30.66));
        spots.add(createSpotInfo("都江堰", 103.65, 31.00));

        @SuppressWarnings("unchecked")
        List<List<Object>> clusters = (List<List<Object>>) method.invoke(travelController, spots);
        assertNotNull(clusters);
        assertTrue(clusters.size() >= 1);
    }

    @Test
    void testGeocodeSpots() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("geocodeSpots", String.class, String.class);
        method.setAccessible(true);

        when(amapService.geo("武侯祠", "成都市")).thenReturn("104.05,30.65");
        when(amapService.geo("锦里", "成都市")).thenReturn("104.06,30.66");

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(travelController, "武侯祠,锦里", "成都市");
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGeocodeSpotsWithChineseComma() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("geocodeSpots", String.class, String.class);
        method.setAccessible(true);

        when(amapService.geo("武侯祠", "成都市")).thenReturn("104.05,30.65");

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(travelController, "武侯祠，锦里", "成都市");
        assertEquals(2, result.size());
    }

    @Test
    void testGeocodeSpotsNullInput() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("geocodeSpots", String.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(travelController, null, "成都市");
        assertTrue(result.isEmpty());

        result = (List<Object>) method.invoke(travelController, "  ", "成都市");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseJsonResult() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("parseJsonResult", String.class, String.class, int.class, int.class, int.class, List.class);
        method.setAccessible(true);

        String aiReply = "{" +
                "  \"days\": [{" +
                "    \"day\": 1," +
                "    \"schedule\": [" +
                "      { \"time\": \"09:00\", \"type\": \"scenic\", \"content\": \"游览武侯祠\", \"location\": \"武侯祠\", \"spotName\": \"武侯祠\" }" +
                "    ]" +
                "  }]," +
                "  \"totalBudget\": 200," +
                "  \"tips\": \"tip\"" +
                "}";

        when(amapService.geo("武侯祠", "成都市")).thenReturn("104.05,30.65");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(travelController, aiReply, "成都市", 2, 1000, 1, new ArrayList<>());
        assertNotNull(result);
        assertEquals("成都市", result.get("city"));
        assertEquals(2, result.get("days"));
        assertEquals(1000, result.get("budget"));
        assertEquals(1, result.get("travelers"));
        assertNotNull(result.get("itinerary"));
        assertNotNull(result.get("scenicSpots"));
        assertEquals(200, result.get("totalBudget"));
    }

    @Test
    void testParseJsonResultEmptyDays() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("parseJsonResult", String.class, String.class, int.class, int.class, int.class, List.class);
        method.setAccessible(true);

        String aiReply = "{\"days\": [], \"totalBudget\": 0}";
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(travelController, aiReply, "成都市", 1, 0, 1, new ArrayList<>());
        assertNull(result);
    }

    @Test
    void testParseJsonResultMalformedJson() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("parseJsonResult", String.class, String.class, int.class, int.class, int.class, List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(travelController, "{{invalid", "成都市", 1, 0, 1, new ArrayList<>());
        assertNull(result);
    }

    @Test
    void testEnsureAllSpotsOnMap() throws Exception {
        Method ensureMethod = TravelController.class.getDeclaredMethod("ensureAllSpotsOnMap", Map.class, List.class);
        ensureMethod.setAccessible(true);

        List<Object> originalSpots = new ArrayList<>();
        originalSpots.add(createSpotInfo("武侯祠", 104.05, 30.65));
        originalSpots.add(createSpotInfo("锦里", 104.06, 30.66));

        List<Map<String, Object>> scenicSpots = new ArrayList<>();
        Map<String, Object> existing = new LinkedHashMap<>();
        existing.put("order", 1);
        existing.put("name", "武侯祠");
        existing.put("day", 1);
        existing.put("lng", 104.05);
        existing.put("lat", 30.65);
        scenicSpots.add(existing);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenicSpots", scenicSpots);

        ensureMethod.invoke(travelController, result, originalSpots);

        assertEquals(2, scenicSpots.size());
        assertEquals("锦里", scenicSpots.get(1).get("name"));
    }

    @Test
    void testEnsureAllSpotsOnMapEmptyOriginals() throws Exception {
        Method ensureMethod = TravelController.class.getDeclaredMethod("ensureAllSpotsOnMap", Map.class, List.class);
        ensureMethod.setAccessible(true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenicSpots", new ArrayList<>());

        ensureMethod.invoke(travelController, result, new ArrayList<>());
        assertEquals(0, ((List<?>) result.get("scenicSpots")).size());
    }

    @Test
    void testBuildSpatialContext() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("buildSpatialContext", List.class);
        method.setAccessible(true);

        List<Object> spots = new ArrayList<>();
        spots.add(createSpotInfo("武侯祠", 104.05, 30.65));
        spots.add(createSpotInfo("锦里", 104.06, 30.66));

        String context = (String) method.invoke(travelController, spots);
        assertNotNull(context);
        assertTrue(context.contains("武侯祠"));
        assertTrue(context.contains("锦里"));
    }

    @Test
    void testBuildSpatialContextEmpty() throws Exception {
        Method method = TravelController.class.getDeclaredMethod("buildSpatialContext", List.class);
        method.setAccessible(true);

        String context = (String) method.invoke(travelController, new ArrayList<>());
        assertEquals("", context);
    }

    // ==================== Helper ====================

    private Object createSpotInfo(String name, Double lng, Double lat) {
        try {
            Class<?> clazz = Class.forName("com.example.ordermanagement.controller.TravelController$SpotInfo");
            return clazz.getDeclaredConstructor(String.class, Double.class, Double.class).newInstance(name, lng, lat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
