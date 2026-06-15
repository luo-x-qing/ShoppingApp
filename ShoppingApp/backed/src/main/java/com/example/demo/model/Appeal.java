package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appeal")
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String shopName;
    private String status;
    private String type;
    private String content;
    private String contact;
    private String reply;
    private String replyStatus;
    private LocalDateTime createTime;
    private LocalDateTime replyTime;
    
    public Appeal() {
        this.createTime = LocalDateTime.now();
        this.replyStatus = "PENDING";
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getReplyStatus() { return replyStatus; }
    public void setReplyStatus(String replyStatus) { this.replyStatus = replyStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getReplyTime() { return replyTime; }
    public void setReplyTime(LocalDateTime replyTime) { this.replyTime = replyTime; }
}