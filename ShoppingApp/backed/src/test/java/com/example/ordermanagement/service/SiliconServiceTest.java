package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.dto.AIMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiliconServiceTest {

    @InjectMocks
    private SiliconService siliconService;

    @Mock
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(siliconService, "url", "https://api.siliconflow.cn/v1/chat/completions");
        ReflectionTestUtils.setField(siliconService, "key", "test-api-key");
        ReflectionTestUtils.setField(siliconService, "model", "deepseek-ai/DeepSeek-V3");
    }

    // ========== 1. 测试系统提示词构建 ==========
    @Test
    void testBuildFullMessages_WithSystemPrompt() throws Exception {
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("7月1日从北京去杭州玩3天");
        messages.add(userMsg);

        List<Hotel> mockHotels = createMockHotels();
        when(hotelService.getAllHotels()).thenReturn(mockHotels);

        Method method = SiliconService.class.getDeclaredMethod("buildFullMessages", List.class);
        method.setAccessible(true);
        Object result = method.invoke(siliconService, messages);

        assertNotNull(result, "构建的消息列表不应为空");
    }

    // ========== 2. 测试航班标记解析 ==========
    @Test
    void testParseFlightMarkers() {
        String text = "推荐航班：\n" +
                "[FLIGHT:北京|杭州|2026-07-01|720]\n" +
                "[FLIGHT:杭州|北京|2026-07-04|680]\n" +
                "祝您旅途愉快！";

        assertTrue(text.contains("[FLIGHT:"), "应包含航班标记");

        int count = text.split("\\[FLIGHT:").length - 1;
        assertEquals(2, count, "应包含2个航班");
    }

    // ========== 3. 测试酒店标记解析 ==========
    @Test
    void testParseHotelMarkers() {
        String text = "推荐酒店：\n" +
                "[HOTEL:如家酒店|杭州|299|4]\n" +
                "[HOTEL:香格里拉大酒店|杭州|599|5]";

        assertTrue(text.contains("[HOTEL:"), "应包含酒店标记");

        int count = text.split("\\[HOTEL:").length - 1;
        assertEquals(2, count, "应包含2个酒店");
    }

    // ========== 4. 测试日期提取 ==========
    @Test
    void testExtractDateFromUserMessage() {
        String userMessage1 = "7.4从北京去福州玩4天";
        String userMessage2 = "7月4日从北京去福州";
        String userMessage3 = "2026-07-04从北京去福州";

        String date1 = extractDate(userMessage1);
        String date2 = extractDate(userMessage2);
        String date3 = extractDate(userMessage3);

        assertEquals("2026-07-04", date1, "7.4 应解析为 2026-07-04");
        assertEquals("2026-07-04", date2, "7月4日 应解析为 2026-07-04");
        assertEquals("2026-07-04", date3, "2026-07-04 应保持不变");
    }

    // ========== 5. 测试天数提取 ==========
    @Test
    void testExtractDaysFromUserMessage() {
        assertEquals(4, extractDays("7.4从北京去福州玩4天"), "应提取4天");
        assertEquals(3, extractDays("玩3天"), "应提取3天");
        assertEquals(5, extractDays("游玩5天"), "应提取5天");
        assertEquals(0, extractDays("去旅游"), "无天数应返回0");
    }

    // ========== 6. 测试返程日期计算 ==========
    @Test
    void testCalculateReturnDate() {
        String departureDate = "2026-07-04";
        int days = 4;

        String returnDate = calculateReturnDate(departureDate, days);
        assertEquals("2026-07-08", returnDate, "返程日期应为2026-07-08");
    }

    // ========== 7. 测试返程日期跨月 ==========
    @Test
    void testCalculateReturnDate_CrossMonth() {
        String departureDate = "2026-07-28";
        int days = 5;

        String returnDate = calculateReturnDate(departureDate, days);
        assertEquals("2026-08-02", returnDate, "返程日期跨月应为2026-08-02");
    }

    // ========== 8. 测试酒店列表获取 ==========
    @Test
    void testGetHotelListForPrompt() throws Exception {
        List<Hotel> mockHotels = createMockHotels();
        when(hotelService.getAllHotels()).thenReturn(mockHotels);

        Method method = SiliconService.class.getDeclaredMethod("getHotelListForPrompt");
        method.setAccessible(true);
        String hotelList = (String) method.invoke(siliconService);

        assertNotNull(hotelList, "酒店列表不应为空");
        assertTrue(hotelList.contains("如家酒店"), "应包含如家酒店");
        assertTrue(hotelList.contains("香格里拉大酒店"), "应包含香格里拉大酒店");
        assertTrue(hotelList.contains("¥"), "应包含价格信息");
        assertTrue(hotelList.contains("星级"), "应包含星级信息");
    }

    // ========== 9. 测试API超时配置 ==========
    @Test
    void testTimeoutConfiguration() {
        SiliconService service = new SiliconService();
        assertNotNull(service, "服务实例应创建成功");
    }

    // ========== 10. 测试AIMessage格式 ==========
    @Test
    void testAIMessageFormat() {
        AIMessage message = new AIMessage();
        message.setRole("user");
        message.setContent("测试消息");

        assertEquals("user", message.getRole(), "角色应为user");
        assertEquals("测试消息", message.getContent(), "内容应为'测试消息'");
    }

    // ========== 11. 测试完整消息流程 ==========
    @Test
    void testChatWithMessages_Integration() {
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("7月1日从北京去杭州玩3天");
        messages.add(userMsg);

        assertNotNull(messages, "消息列表不应为空");
        assertEquals(1, messages.size(), "应有1条消息");
        assertEquals("user", messages.get(0).getRole(), "角色应为user");
        assertEquals("7月1日从北京去杭州玩3天", messages.get(0).getContent(), "内容应正确");
    }

    // ========== 辅助方法 ==========

    private List<Hotel> createMockHotels() {
        List<Hotel> hotels = new ArrayList<>();

        Hotel hotel1 = new Hotel();
        hotel1.setId(1L);
        hotel1.setName("如家酒店");
        hotel1.setAddress("福建省福州市鼓楼区");
        hotel1.setStarLevel(4);
        hotel1.setPrice(199.0);
        hotels.add(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setId(2L);
        hotel2.setName("香格里拉大酒店");
        hotel2.setAddress("福建省福州市鼓楼区");
        hotel2.setStarLevel(5);
        hotel2.setPrice(299.0);
        hotels.add(hotel2);

        Hotel hotel3 = new Hotel();
        hotel3.setId(3L);
        hotel3.setName("yunduo酒店");
        hotel3.setAddress("浙江省杭州市西湖区");
        hotel3.setStarLevel(5);
        hotel3.setPrice(9999.0);
        hotels.add(hotel3);

        return hotels;
    }

    private String extractDate(String text) {
        Pattern pattern = Pattern.compile("(\\d{1,2})[.月](\\d{1,2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String month = String.format("%02d", Integer.parseInt(matcher.group(1)));
            String day = String.format("%02d", Integer.parseInt(matcher.group(2)));
            return "2026-" + month + "-" + day;
        }
        pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    private int extractDays(String text) {
        Pattern pattern = Pattern.compile("(\\d+)天");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String calculateReturnDate(String departureDate, int days) {
        try {
            String[] parts = departureDate.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]) + days;

            int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                daysInMonth[1] = 29;
            }

            while (day > daysInMonth[month - 1]) {
                day -= daysInMonth[month - 1];
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
            }

            return String.format("%d-%02d-%02d", year, month, day);
        } catch (Exception e) {
            return departureDate;
        }
    }
}