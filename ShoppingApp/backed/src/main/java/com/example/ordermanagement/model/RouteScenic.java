package com.example.ordermanagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "route_scenic")
public class RouteScenic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    @JsonBackReference
    private TourRoute tourRoute;

    @ManyToOne
    @JoinColumn(name = "scenic_id", nullable = false)
    private Scenic scenic;

    private Integer dayNumber;

    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TourRoute getTourRoute() { return tourRoute; }
    public void setTourRoute(TourRoute tourRoute) { this.tourRoute = tourRoute; }
    public Scenic getScenic() { return scenic; }
    public void setScenic(Scenic scenic) { this.scenic = scenic; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
