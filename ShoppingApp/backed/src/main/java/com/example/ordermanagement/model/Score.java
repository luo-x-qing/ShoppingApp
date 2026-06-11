package com.example.ordermanagement.model;
import javax.persistence.*;

@Entity
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long attractionId;
    private Integer score; // 1-5分

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAttractionId() { return attractionId; }
    public void setAttractionId(Long attractionId) { this.attractionId = attractionId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}