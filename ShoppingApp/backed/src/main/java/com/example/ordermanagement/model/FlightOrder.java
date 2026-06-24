package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_order")
public class FlightOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long flightId;

    private String flightNumber;
    private String departCity;
    private String arriveCity;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private Double price;

    @Column(name = "username")
    private String username;
    private String passengerName;
    private String passengerIdCard;
    private String contactPhone;

    private String status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        if (this.status == null) this.status = "待支付";  // 改为待支付，不是已出票
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
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
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getPassengerIdCard() { return passengerIdCard; }
    public void setPassengerIdCard(String passengerIdCard) { this.passengerIdCard = passengerIdCard; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
}