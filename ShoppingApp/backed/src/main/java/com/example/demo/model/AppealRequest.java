package com.example.demo.model;

public class AppealRequest {
    private String username;
    private String shopName;
    private String status;
    private String type;
    private String content;
    private String contact;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}