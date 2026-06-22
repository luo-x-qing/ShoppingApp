package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.SystemNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface SystemNoticeRepository extends JpaRepository<SystemNotice, Long> {
    
    // 获取用户所有通知（按时间倒序）
    List<SystemNotice> findByUsernameOrderByCreateTimeDesc(String username);
    
    // 获取用户未读通知
    List<SystemNotice> findByUsernameAndIsReadFalse(String username);
    
    // 检查订单是否已生成过通知
    boolean existsByOrderIdAndType(Long orderId, String type);
    
    // 标记为已读
    @Modifying
    @Transactional
    @Query("UPDATE SystemNotice n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    // 批量标记为已读
    @Modifying
    @Transactional
    @Query("UPDATE SystemNotice n SET n.isRead = true WHERE n.username = :username")
    void markAllAsRead(@Param("username") String username);
    
    // 删除旧通知（超过30天）
    @Modifying
    @Transactional
    @Query("DELETE FROM SystemNotice n WHERE n.createTime < :date")
    int deleteOldNotices(@Param("date") LocalDateTime date);
}