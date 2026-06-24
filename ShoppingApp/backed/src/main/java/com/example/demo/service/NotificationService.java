package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * 创建通知
     */
    @Transactional
    public Notification createNotification(Long merchantId, String merchantName, 
                                           String type, String title, String content,
                                           String relatedId, String relatedName) {
        Notification notification = new Notification();
        notification.setMerchantId(merchantId);
        notification.setMerchantName(merchantName);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedName(relatedName);
        notification.setCreateTime(LocalDateTime.now());
        notification.setStatus("UNREAD");
        return notificationRepository.save(notification);
    }

    /**
     * 获取商家的所有通知
     */
    public List<Notification> getMerchantNotifications(Long merchantId) {
        return notificationRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId);
    }

    /**
     * 获取商家未读通知数量
     */
    public long getUnreadCount(Long merchantId) {
        return notificationRepository.countByMerchantIdAndStatus(merchantId, "UNREAD");
    }

    /**
     * 标记单条通知为已读
     */
    @Transactional
    public void markAsRead(Long merchantId, Long notificationId) {
        notificationRepository.markAsRead(merchantId, notificationId);
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long merchantId) {
        notificationRepository.markAllAsRead(merchantId);
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    /**
     * 商家被禁用时发送通知
     */
    public void sendMerchantBannedNotification(Long merchantId, String merchantName, String reason) {
        createNotification(
            merchantId, merchantName,
            "MERCHANT_BANNED",
            "账号已被禁用",
            "您的商家账号已被管理员禁用，原因：" + (reason != null ? reason : "违规经营") + "。如有疑问，请通过申诉功能联系管理员。",
            String.valueOf(merchantId), merchantName
        );
    }

    /**
     * 商家被解禁时发送通知
     */
    public void sendMerchantUnbannedNotification(Long merchantId, String merchantName) {
        createNotification(
            merchantId, merchantName,
            "MERCHANT_UNBANNED",
            "账号已恢复",
            "您的商家账号已恢复正常使用，感谢您的配合。",
            String.valueOf(merchantId), merchantName
        );
    }

    /**
     * 酒店被禁用时发送通知
     */
    public void sendHotelBannedNotification(Long merchantId, String merchantName, 
                                            Long hotelId, String hotelName, String reason) {
        createNotification(
            merchantId, merchantName,
            "HOTEL_BANNED",
            "酒店已被禁用",
            "您的酒店「" + hotelName + "」已被管理员禁用，原因：" + (reason != null ? reason : "违规经营") + "。请及时处理。",
            String.valueOf(hotelId), hotelName
        );
    }

    /**
     * 酒店被解禁时发送通知
     */
    public void sendHotelUnbannedNotification(Long merchantId, String merchantName, 
                                              Long hotelId, String hotelName) {
        createNotification(
            merchantId, merchantName,
            "HOTEL_UNBANNED",
            "酒店已恢复",
            "您的酒店「" + hotelName + "」已恢复正常营业。",
            String.valueOf(hotelId), hotelName
        );
    }

    /**
     * 申诉通过时发送通知
     */
    public void sendAppealApprovedNotification(Long merchantId, String merchantName, String appealContent) {
        createNotification(
                merchantId, merchantName,
                "APPEAL_APPROVED",
                "申诉已通过",
                "您的申诉已通过审核。" + (appealContent != null ? "申诉内容：" + appealContent : "") + "商家账号已恢复正常。",
                String.valueOf(merchantId), merchantName
        );
    }

    /**
     * 申诉拒绝时发送通知
     */
    public void sendAppealRejectedNotification(Long merchantId, String merchantName, String rejectReason) {
        createNotification(
                merchantId, merchantName,
                "APPEAL_REJECTED",
                "申诉未通过",
                "您的申诉未被通过，原因：" + (rejectReason != null ? rejectReason : "不符合申诉条件") + "。如有疑问请联系管理员。",
                String.valueOf(merchantId), merchantName
        );
    }

    /**
     * 申诉回复通知（包含管理员回复内容）
     */
    public void sendAppealReplyNotification(Long merchantId, String merchantName,
                                            String title, String content, String reply) {
        Notification notification = new Notification();
        notification.setMerchantId(merchantId);
        notification.setMerchantName(merchantName);
        notification.setType("APPEAL_REPLY");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReply(reply);  // 保存管理员回复
        notification.setRelatedId(String.valueOf(merchantId));
        notification.setRelatedName(merchantName);
        notification.setCreateTime(LocalDateTime.now());
        notification.setStatus("UNREAD");
        notificationRepository.save(notification);
    }

    /**
     * 用户被禁言时发送通知
     */
    public void sendUserBannedNotification(Long userId, String username, String reason) {
        createUserNotification(
                userId, username,
                "USER_BANNED",
                "账号已被禁言",
                "您的账号已被管理员禁言，原因：" + (reason != null ? reason : "违规发言") + "。禁言期间无法发布评论和评价。",
                String.valueOf(userId), username
        );
    }

    /**
     * 用户被解禁时发送通知
     */
    public void sendUserUnbannedNotification(Long userId, String username) {
        createUserNotification(
                userId, username,
                "USER_UNBANNED",
                "账号已解禁",
                "您的账号已恢复正常，可以继续发布评论和评价了。",
                String.valueOf(userId), username
        );
    }

    /**
     * 创建用户通知
     */
    public void createUserNotification(Long userId, String username,
                                       String type, String title, String content,
                                       String relatedId, String relatedName) {
        Notification notification = new Notification();
        notification.setMerchantId(userId);  // 复用字段，存储用户ID
        notification.setMerchantName(username);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedName(relatedName);
        notification.setCreateTime(LocalDateTime.now());
        notification.setStatus("UNREAD");
        notificationRepository.save(notification);
    }

    // 获取用户的通知
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByMerchantIdOrderByCreateTimeDesc(userId);
    }

    // 获取用户未读数量
    public long getUserUnreadCount(Long userId) {
        return notificationRepository.countByMerchantIdAndStatus(userId, "UNREAD");
    }

    // 标记用户通知为已读
    public void markUserNotificationAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
    }

    // 标记所有用户通知为已读
    public void markAllUserNotificationsAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // 用户被禁言通知（包含时长）
    public void sendUserBannedNotification(Long userId, String username, String reason, int durationDays) {
        String durationText = durationDays > 0 ? durationDays + "天" : "永久";
        createUserNotification(
                userId, username,
                "USER_BANNED",
                "账号已被禁言",
                "您的账号已被管理员禁言" + durationText + "，原因：" + reason + "。禁言期间无法发布评论和评价。",
                String.valueOf(userId), username
        );
    }
}