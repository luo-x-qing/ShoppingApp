package com.example.ordermanagement.service;
import com.example.ordermanagement.model.Score;
import com.example.ordermanagement.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ScoreService {
    @Autowired private ScoreRepository scoreRepo;
    public Score save(Score s){return scoreRepo.save(s);}
    public Double getAvg(Long aid){
        Optional<Double> avg = scoreRepo.getAvgScore(aid);
        return avg.orElse(null);
    }
}