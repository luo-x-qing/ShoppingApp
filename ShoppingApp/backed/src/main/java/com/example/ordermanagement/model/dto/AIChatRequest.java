package com.example.ordermanagement.model.dto;

import java.util.List;

public class AIChatRequest {
    private List<AIMessage> messages;
    private String username;

    public List<AIMessage> getMessages() { return messages; }
    public void setMessages(List<AIMessage> messages) { this.messages = messages; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}