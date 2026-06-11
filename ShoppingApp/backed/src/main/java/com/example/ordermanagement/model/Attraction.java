package com.example.ordermanagement.model;

import javax.persistence.*;

@Entity
@Table(name = "attraction")
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String province;
    private String city;
    private String photo;
    private Double score; // 总评分
    @Column(columnDefinition = "TEXT")
    private String description;

    // 👇 下面这两个必须加上
    public Double getScore() {
        return score;
    }
    public void setScore(Double score) {
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}