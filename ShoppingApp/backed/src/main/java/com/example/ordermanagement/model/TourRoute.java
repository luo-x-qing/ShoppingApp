package com.example.ordermanagement.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_route")
public class TourRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String intro;

    private Integer days;

    private Double price;

    private Double distance;

    private String duration;

    @Column(columnDefinition = "TEXT")
    private String serviceInclude;

    @Column(columnDefinition = "TEXT")
    private String serviceExclude;

    private String images;

    @Column(columnDefinition = "TEXT")
    private String notice;

    private Long merchantId;

    private Integer auditStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "tourRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RouteScenic> routeScenics = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getServiceInclude() { return serviceInclude; }
    public void setServiceInclude(String serviceInclude) { this.serviceInclude = serviceInclude; }
    public String getServiceExclude() { return serviceExclude; }
    public void setServiceExclude(String serviceExclude) { this.serviceExclude = serviceExclude; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getNotice() { return notice; }
    public void setNotice(String notice) { this.notice = notice; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<RouteScenic> getRouteScenics() { return routeScenics; }
    public void setRouteScenics(List<RouteScenic> routeScenics) { this.routeScenics = routeScenics; }
}
