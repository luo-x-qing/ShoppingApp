package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.dto.AIChatRequest;
import com.example.ordermanagement.model.dto.AIChatResponse;
import com.example.ordermanagement.service.SiliconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {

    @Autowired
    private SiliconService siliconService;

    @PostMapping("/chat")
    public AIChatResponse chat(@RequestBody AIChatRequest request) {
        String reply = siliconService.chatWithMessages(request.getMessages());
        AIChatResponse response = new AIChatResponse();
        response.setReply(reply);
        return response;
    }
}
