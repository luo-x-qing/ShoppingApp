package com.example.ordermanagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long attractionId;
    private String content;
    private LocalDateTime createTime;
    private Integer score; // 1-5星

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }

    // getter setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttractionId() { return attractionId; }
    public void setAttractionId(Long attractionId) { this.attractionId = attractionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    // 👇 下面这两个就是你缺的，加上就不报错了
    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }
}