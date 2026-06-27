package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.dto.AIChatRequest;
import com.example.ordermanagement.model.dto.AIChatResponse;
import com.example.ordermanagement.model.dto.AIMessage;
import com.example.ordermanagement.service.SiliconService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIChatControllerTest {

    @InjectMocks
    private AIChatController controller;

    @Mock
    private SiliconService siliconService;

    // 移除 setUp 中的全局 mock，改为在每个测试中单独 mock

    @Test
    void testChat_Success() {
        // 在测试中单独 mock
        when(siliconService.chatWithMessages(any()))
                .thenReturn("这是AI的回复内容：为您规划了3天的杭州行程...");

        AIChatRequest request = new AIChatRequest();
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("7月1日从北京去杭州玩3天");
        messages.add(userMsg);
        request.setMessages(messages);
        request.setUsername("testUser");

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
        assertTrue(response.getReply().contains("AI"), "回复应包含'AI'");
    }

    @Test
    void testChat_EmptyMessages() {
        // 即使消息为空，也需要 mock
        when(siliconService.chatWithMessages(any()))
                .thenReturn("这是AI的回复内容");

        AIChatRequest request = new AIChatRequest();
        request.setMessages(new ArrayList<>());
        request.setUsername("testUser");

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
    }

    @Test
    void testChat_SingleMessage() {
        when(siliconService.chatWithMessages(any()))
                .thenReturn("为您推荐福州的酒店：如家酒店、香格里拉大酒店");

        AIChatRequest request = new AIChatRequest();
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("福州有哪些酒店？");
        messages.add(userMsg);
        request.setMessages(messages);
        request.setUsername("testUser");

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
        assertTrue(response.getReply().contains("酒店"), "回复应包含酒店");
    }

    @Test
    void testChat_MultipleMessages() {
        when(siliconService.chatWithMessages(any()))
                .thenReturn("好的，为您规划3天的杭州行程...");

        AIChatRequest request = new AIChatRequest();
        List<AIMessage> messages = new ArrayList<>();

        AIMessage userMsg1 = new AIMessage();
        userMsg1.setRole("user");
        userMsg1.setContent("我想去杭州旅游");
        messages.add(userMsg1);

        AIMessage assistantMsg = new AIMessage();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent("好的，请问您计划去几天？");
        messages.add(assistantMsg);

        AIMessage userMsg2 = new AIMessage();
        userMsg2.setRole("user");
        userMsg2.setContent("3天");
        messages.add(userMsg2);

        request.setMessages(messages);
        request.setUsername("testUser");

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
    }

    @Test
    void testChat_NullUsername() {
        when(siliconService.chatWithMessages(any()))
                .thenReturn("AI回复内容");

        AIChatRequest request = new AIChatRequest();
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("测试消息");
        messages.add(userMsg);
        request.setMessages(messages);
        request.setUsername(null);

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
    }

    @Test
    void testChat_EmptyUsername() {
        when(siliconService.chatWithMessages(any()))
                .thenReturn("AI回复内容");

        AIChatRequest request = new AIChatRequest();
        List<AIMessage> messages = new ArrayList<>();
        AIMessage userMsg = new AIMessage();
        userMsg.setRole("user");
        userMsg.setContent("测试消息");
        messages.add(userMsg);
        request.setMessages(messages);
        request.setUsername("");

        AIChatResponse response = controller.chat(request);

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getReply(), "回复内容不应为空");
    }

    @Test
    void testCorsAnnotation() {
        assertTrue(AIChatController.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.CrossOrigin.class),
                "Controller应有@CrossOrigin注解");
    }

    @Test
    void testRequestMappingPath() {
        org.springframework.web.bind.annotation.RequestMapping mapping =
                AIChatController.class.getAnnotation(
                        org.springframework.web.bind.annotation.RequestMapping.class
                );
        assertNotNull(mapping, "应有@RequestMapping注解");
        assertEquals("/api/ai", mapping.value()[0], "路径应为/api/ai");
    }
}