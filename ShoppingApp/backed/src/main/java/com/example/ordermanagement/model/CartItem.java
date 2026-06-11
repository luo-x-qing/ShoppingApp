package com.example.ordermanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @Column(nullable = false)
    private Long scenicId;

    private String scenicName;

    private Double ticketPrice;

    private Integer quantity;

    private Long routeId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Long getScenicId() { return scenicId; }
    public void setScenicId(Long scenicId) { this.scenicId = scenicId; }
    public String getScenicName() { return scenicName; }
    public void setScenicName(String scenicName) { this.scenicName = scenicName; }
    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
}
