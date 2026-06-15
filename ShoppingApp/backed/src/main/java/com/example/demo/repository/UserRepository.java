package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByToken(String token);
    List<User> findByRole(String role);
    List<User> findByRoleAndStatus(String role, String status);
    List<User> findByStatusAndBanExpireTimeBefore(String status, LocalDateTime time);
}