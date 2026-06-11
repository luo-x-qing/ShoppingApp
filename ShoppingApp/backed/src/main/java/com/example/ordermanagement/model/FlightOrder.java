package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_order")
public class FlightOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private String departCity;
    private String arriveCity;
    private String departTime;
    private String arriveTime;
    private String userName;
    private String userPhone;
    private Double price;
    private LocalDateTime createTime;

    // ================================
    // 🔥 改名！避免与 userName 冲突
    // ================================
    private String loginUsername;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getDepartCity() { return departCity; }
    public String getDepartCity(String departCity) { return this.departCity; }
    public void setDepartCity(String departCity) { this.departCity = departCity; }
    public String getArriveCity() { return arriveCity; }
    public void setArriveCity(String arriveCity) { this.arriveCity = arriveCity; }
    public String getDepartTime() { return departTime; }
    public void setDepartTime(String departTime) { this.departTime = departTime; }
    public String getArriveTime() { return arriveTime; }
    public void setArriveTime(String arriveTime) { this.arriveTime = arriveTime; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}