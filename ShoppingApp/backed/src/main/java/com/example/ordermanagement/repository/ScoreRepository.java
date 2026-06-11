package com.example.ordermanagement.repository;
import com.example.ordermanagement.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score,Long> {
    @Query("select avg(s.score) from Score s where s.attractionId=:aid")
    Optional<Double> getAvgScore(@Param("aid") Long aid);
}