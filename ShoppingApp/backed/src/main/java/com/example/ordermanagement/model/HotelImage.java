package com.example.ordermanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
@Table(name = "hotel_image")
public class HotelImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // 图片URL

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore // 序列化时忽略，避免循环引用
    private Hotel hotel;

    // 添加一个无参构造函数（JPA 需要）
    public HotelImage() {}

    // 添加一个带参数的构造函数（方便创建对象）
    public HotelImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
}