package com.example.ordermanagement.model;

import javax.persistence.*;
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

    // 酒店分类
    private String category;

    // 🔥 新增：酒店地址
    private String address;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelImage> detailImages = new ArrayList<>();

    // Getters & Setters
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

    // 🔥 地址 GET SET
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<HotelImage> getDetailImages() { return detailImages; }
    public void setDetailImages(List<HotelImage> detailImages) { this.detailImages = detailImages; }

    public void addDetailImage(HotelImage image) {
        detailImages.add(image);
        image.setHotel(this);
    }
}