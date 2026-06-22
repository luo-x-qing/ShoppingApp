package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.username = :username AND m.merchantId = :merchantId) OR (m.username = :username AND m.hotelId = :hotelId) ORDER BY m.createTime ASC")
    List<Message> findChatMessages(@Param("username") String username, 
                                    @Param("merchantId") String merchantId,
                                    @Param("hotelId") Long hotelId);
    
    @Query("SELECT DISTINCT m.merchantId, m.hotelId FROM Message m WHERE m.username = :username")
    List<Object[]> findUserSessions(@Param("username") String username);
    
    @Query("SELECT DISTINCT m.username, m.orderId FROM Message m WHERE m.merchantId = :merchantId")
    List<Object[]> findMerchantSessions(@Param("merchantId") String merchantId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.merchantId = :merchantId AND m.username = :username AND m.senderRole = 'user' AND m.isRead = 0")
    Integer countUnreadForMerchant(@Param("merchantId") String merchantId, @Param("username") String username);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.username = :username AND m.merchantId = :merchantId AND m.senderRole = 'merchant' AND m.isRead = 0")
    Integer countUnreadForUser(@Param("username") String username, @Param("merchantId") String merchantId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.merchantId = :merchantId AND m.username = :username AND m.senderRole = 'user'")
    void markAsReadForMerchant(@Param("merchantId") String merchantId, @Param("username") String username);
    
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.username = :username AND m.merchantId = :merchantId AND m.senderRole = 'merchant'")
    void markAsReadForUser(@Param("username") String username, @Param("merchantId") String merchantId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.merchantId = :merchantId AND m.senderRole = 'user' AND m.isRead = 0")
    Integer countTotalUnreadForMerchant(@Param("merchantId") String merchantId);
}
