package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private String airline;
    private String departCity;
    private String arriveCity;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private Double price;
    private Integer remainingSeats;
    private String status;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "有效";
        if (remainingSeats == null) remainingSeats = 100;
    }

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
