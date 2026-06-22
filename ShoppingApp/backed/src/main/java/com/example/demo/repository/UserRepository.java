package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByToken(String token);

    List<User> findByRole(String role);

    List<User> findByRoleAndStatus(String role, String status);

    List<User> findByStatusAndBanExpireTimeBefore(String status, LocalDateTime time);

    long countByRole(String role);

    long countByRoleAndStatus(String role, String status);

    List<User> findByRoleAndUsernameContaining(String role, String username);

    List<User> findByRoleAndStatusAndUsernameContaining(String role, String status, String username);

    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.role = :role GROUP BY u.status")
    List<Object[]> countByRoleGroupByStatus(@Param("role") String role);

    List<User> findByRoleOrderByCreateTimeDesc(String role);

    List<User> findByRoleAndStatusOrderByCreateTimeDesc(String role, String status);
}
