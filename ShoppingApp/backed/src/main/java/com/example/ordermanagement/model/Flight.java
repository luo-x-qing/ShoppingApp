package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String flightNumber;      // 航班号，如 CA1234
    private String airline;           // 航空公司
    private String departCity;        // 出发城市
    private String arriveCity;        // 到达城市
    private LocalDateTime departTime; // 出发时间
    private LocalDateTime arriveTime; // 到达时间
    private Double price;             // 价格
    private Integer remainingSeats;   // 剩余座位数
    private String status;            // 有效/已失效
    
    @PrePersist
    public void prePersist() {
        if (status == null) status = "有效";
        if (remainingSeats == null) remainingSeats = 100;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }
    public String getDepartCity() { return departCity; }
    public void setDepartCity(String departCity) { this.departCity = departCity; }
    public String getArriveCity() { return arriveCity; }
    public void setArriveCity(String arriveCity) { this.arriveCity = arriveCity; }
    public LocalDateTime getDepartTime() { return departTime; }
    public void setDepartTime(LocalDateTime departTime) { this.departTime = departTime; }
    public LocalDateTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalDateTime arriveTime) { this.arriveTime = arriveTime; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getRemainingSeats() { return remainingSeats; }
    public void setRemainingSeats(Integer remainingSeats) { this.remainingSeats = remainingSeats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}