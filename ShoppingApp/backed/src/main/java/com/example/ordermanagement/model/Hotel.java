package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotel")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer starLevel;

    @Column(nullable = false)
    private Double price;

    private String coverImage;
    private String category;
    private String address;

    // ========== 新增字段 ==========
    private Double latitude;          // 纬度（用于距离计算）
    private Double longitude;         // 经度
    private Double avgRating;         // 平均评分（0-5分）
    private Integer totalRooms;       // 酒店总房间数
    private Long merchantId;          // 商家用户ID（关联商家）
    private String status;            // 营业中/已停业

    // 违规原因
    private String violationReason;

    // 更新时间
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelImage> detailImages = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomType> roomTypes = new ArrayList<>();  // 房间类型列表

    // ========== 生命周期回调方法 ==========
    @PrePersist
    protected void onCreate() {
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = "营业中";
        }
        if (totalRooms == null) {
            totalRooms = 0;
        }
        if (avgRating == null) {
            avgRating = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getViolationReason() { return violationReason; }
    public void setViolationReason(String violationReason) { this.violationReason = violationReason; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStarLevel() { return starLevel; }
    public void setStarLevel(Integer starLevel) { this.starLevel = starLevel; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public Integer getTotalRooms() { return totalRooms; }
    public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<HotelImage> getDetailImages() { return detailImages; }
    public void setDetailImages(List<HotelImage> detailImages) { this.detailImages = detailImages; }
    public List<RoomType> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<RoomType> roomTypes) { this.roomTypes = roomTypes; }
}