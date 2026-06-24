package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotelcomment")
public class HotelComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private Long orderId;
    private String username;          // 评价人用户名
    private String content;
    private Integer score;            // 1-5分

    @Column(columnDefinition = "TEXT")
    private String images;            // 图片URL列表，JSON格式存储 ["url1","url2"]

    private String status;            // 正常/违规（敏感词过滤）
    private LocalDateTime createTime;

    // ========== 新增字段 ==========
    @Column(columnDefinition = "TEXT")
    private String reply;             // 商家回复内容
    private LocalDateTime replyTime;  // 回复时间

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        if (status == null) status = "正常";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public LocalDateTime getReplyTime() { return replyTime; }
    public void setReplyTime(LocalDateTime replyTime) { this.replyTime = replyTime; }
}