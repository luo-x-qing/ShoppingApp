package com.example.demo.repository;

import com.example.demo.model.Appeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AppealRepository extends JpaRepository<Appeal, Long> {
    List<Appeal> findByUsername(String username);
    List<Appeal> findByReplyStatus(String replyStatus);
    List<Appeal> findByType(String type);
    List<Appeal> findByUsernameAndReplyStatus(String username, String replyStatus);
    
    @Query("SELECT COUNT(a) FROM Appeal a WHERE a.replyStatus = 'PENDING'")
    long countPending();
    
    @Query("SELECT COUNT(a) FROM Appeal a WHERE a.replyStatus = 'PROCESSED'")
    long countProcessed();
    
    @Query("SELECT COUNT(a) FROM Appeal a WHERE a.replyStatus = 'REJECTED'")
    long countRejected();
}