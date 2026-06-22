package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_notice")
public class SystemNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;        // 接收通知的用户名
    
    @Column(nullable = false)
    private String type;             // 通知类型：HOTEL_ORDER, FLIGHT_ORDER
    
    @Column(nullable = false)
    private Long orderId;            // 关联的订单ID
    
    @Column(nullable = false)
    private String title;            // 通知标题
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;          // 通知内容
    
    private String emoji;            // 图标emoji
    
    @Column(nullable = false)
    private Boolean isRead = false;  // 是否已读
    
    @Column(nullable = false)
    private LocalDateTime createTime; // 创建时间
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}