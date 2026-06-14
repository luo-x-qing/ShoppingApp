package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByMerchantIdOrderByCreateTimeDesc(Long merchantId);
    
    List<Notification> findByMerchantIdAndStatus(Long merchantId, String status);
    
    long countByMerchantIdAndStatus(Long merchantId, String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.merchantId = :merchantId AND n.id = :notificationId")
    void markAsRead(Long merchantId, Long notificationId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.merchantId = :merchantId")
    void markAllAsRead(Long merchantId);
}
