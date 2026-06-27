package com.example.ordermanagement.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DateParserTest {

    // ========== 1. 测试"月.日"格式 ==========
    @Test
    void testParseDate_DotFormat() {
        String input = "7.4从北京去福州";
        String result = parseDate(input);
        assertEquals("2026-07-04", result, "7.4 应解析为 2026-07-04");
    }

    // ========== 2. 测试"月日"格式（无分隔符）- 应该返回null ==========
    @Test
    void testParseDate_NoDotFormat() {
        String input = "74从北京去福州";
        String result = parseDate(input);
        assertNull(result, "无分隔符应返回null");
    }

    // ========== 3. 测试"月月日"格式 ==========
    @Test
    void testParseDate_MonthDayFormat() {
        String input = "7月4日从北京去福州";
        String result = parseDate(input);
        assertEquals("2026-07-04", result, "7月4日 应解析为 2026-07-04");
    }

    // ========== 4. 测试ISO格式 ==========
    @Test
    void testParseDate_ISOFormat() {
        String input = "2026-07-04从北京去福州";
        String result = parseDate(input);
        assertEquals("2026-07-04", result, "2026-07-04 应保持不变");
    }

    // ========== 5. 测试斜杠格式 ==========
    @Test
    void testParseDate_SlashFormat() {
        String input = "2026/07/04从北京去福州";
        String result = parseDate(input);
        assertEquals("2026-07-04", result, "2026/07/04 应转换为 2026-07-04");
    }

    // ========== 6. 测试无效日期 ==========
    @Test
    void testParseDate_Invalid() {
        String input = "今天去福州";
        String result = parseDate(input);
        assertNull(result, "无效日期应返回null");
    }

    // ========== 7. 测试跨年日期（1月） ==========
    @Test
    void testParseDate_CrossYear_January() {
        String input = "1.4从北京去福州";
        String result = parseDateWithYear(input);
        assertEquals("2027-01-04", result, "1月应解析为2027年");
    }

    // ========== 8. 测试跨年日期（2月） ==========
    @Test
    void testParseDate_CrossYear_February() {
        String input = "2.4从北京去福州";
        String result = parseDateWithYear(input);
        assertEquals("2027-02-04", result, "2月应解析为2027年");
    }

    // ========== 9. 测试当年日期（3月以后） ==========
    @Test
    void testParseDate_CurrentYear() {
        String input = "6.4从北京去福州";
        String result = parseDateWithYear(input);
        assertEquals("2026-06-04", result, "6月应解析为2026年");
    }

    // ========== 10. 测试天数提取 ==========
    @Test
    void testExtractDays() {
        assertEquals(3, extractDays("玩3天"), "应提取3天");
        assertEquals(5, extractDays("游玩5天"), "应提取5天");
        assertEquals(1, extractDays("1天"), "应提取1天");
        assertEquals(0, extractDays("没有天数"), "无天数应返回0");
        assertEquals(7, extractDays("7天游"), "应提取7天");
    }

    // ========== 11. 测试返程日期计算（同月） ==========
    @Test
    void testCalculateReturnDate_SameMonth() {
        String departure = "2026-07-04";
        int days = 4;
        String returnDate = calculateReturnDate(departure, days);
        assertEquals("2026-07-08", returnDate, "返程日期应为2026-07-08");
    }

    // ========== 12. 测试返程日期计算（跨月） ==========
    @Test
    void testCalculateReturnDate_CrossMonth() {
        String departure = "2026-07-28";
        int days = 5;
        String returnDate = calculateReturnDate(departure, days);
        assertEquals("2026-08-02", returnDate, "返程日期跨月应为2026-08-02");
    }

    // ========== 13. 测试返程日期计算（跨年） ==========
    @Test
    void testCalculateReturnDate_CrossYear() {
        String departure = "2026-12-28";
        int days = 5;
        String returnDate = calculateReturnDate(departure, days);
        assertEquals("2027-01-02", returnDate, "返程日期跨年应为2027-01-02");
    }

    // ========== 辅助方法 ==========

    private String parseDate(String text) {
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
        pattern = Pattern.compile("(\\d{4})/(\\d{2})/(\\d{2})");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3);
        }
        return null;
    }

    private String parseDateWithYear(String text) {
        Pattern pattern = Pattern.compile("(\\d{1,2})[.月](\\d{1,2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int month = Integer.parseInt(matcher.group(1));
            int day = Integer.parseInt(matcher.group(2));
            int year = 2026;
            if (month <= 2) {
                year = 2027;
            }
            return String.format("%d-%02d-%02d", year, month, day);
        }
        return null;
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