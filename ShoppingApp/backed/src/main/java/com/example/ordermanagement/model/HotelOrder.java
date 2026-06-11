package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotelorder")
public class HotelOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private String name;
    private Double price;
    private String checkIn;
    private String checkOut;
    private LocalDateTime createTime;
    private String comment;

    // ======================
    // 🔥 强制指定数据库字段名：username
    // ======================
    @Column(name = "username")
    private String username;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCheckIn() { return checkIn; }
    public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
    public String getCheckOut() { return checkOut; }
    public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}