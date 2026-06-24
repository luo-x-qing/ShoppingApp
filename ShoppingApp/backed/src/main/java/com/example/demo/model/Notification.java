package com.example.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long merchantId;      // 商家ID
    private String merchantName;   // 商家名称
    private String type;           // 通知类型：MERCHANT_BANNED, HOTEL_BANNED, HOTEL_VIOLATION
    private String title;          // 通知标题
    private String content;        // 通知内容
    private String relatedId;      // 关联ID（如酒店ID）
    private String relatedName;    // 关联名称（如酒店名称）
    private String status;         // 状态：UNREAD, READ
    private LocalDateTime createTime;
    // 在类中添加
    private String reply;  // 管理员回复内容

    
    public Notification() {
        this.createTime = LocalDateTime.now();
        this.status = "UNREAD";
    }


    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    
    public String getRelatedName() { return relatedName; }
    public void setRelatedName(String relatedName) { this.relatedName = relatedName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}