package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scenic")
public class Scenic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String province;

    private String city;

    private String photo;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double score;

    private Double ticketPrice;

    private Integer auditStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

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
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }
    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
