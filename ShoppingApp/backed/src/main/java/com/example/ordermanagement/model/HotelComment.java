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
    private String content;
    private Integer score;
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }

    // getter & setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}