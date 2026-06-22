package com.example.ordermanagement.service;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.SystemNotice;
import com.example.ordermanagement.repository.SystemNoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemNoticeService {

    @Autowired
    private SystemNoticeRepository systemNoticeRepository;

    /**
     * 创建酒店订单通知
     */
    @Transactional
    public SystemNotice createHotelOrderNotice(HotelOrder order, String hotelName) {
        // 检查是否已存在相同订单的通知
        if (systemNoticeRepository.existsByOrderIdAndType(order.getId(), "HOTEL_ORDER")) {
            return null;
        }

        SystemNotice notice = new SystemNotice();
        notice.setUsername(order.getUsername());
        notice.setType("HOTEL_ORDER");
        notice.setOrderId(order.getId());
        notice.setTitle("酒店预订成功");
        notice.setEmoji("🏨");
        notice.setContent(String.format(
            "您预订的%s已确认！入住日期：%s，退房日期：%s，金额：¥%.2f。祝您入住愉快！",
            hotelName != null ? hotelName : "酒店",
            order.getCheckIn() != null ? order.getCheckIn() : "待确认",
            order.getCheckOut() != null ? order.getCheckOut() : "待确认",
            order.getPrice() != null ? order.getPrice() : 0
        ));
        notice.setIsRead(false);

        return systemNoticeRepository.save(notice);
    }

    /**
     * 创建机票订单通知
     */
    @Transactional
    public SystemNotice createFlightOrderNotice(String username, Long orderId,
                                                  String fromCity, String toCity,
                                                  String flightNo, String departureTime,
                                                  Double price) {
        // 检查是否已存在相同订单的通知
        if (systemNoticeRepository.existsByOrderIdAndType(orderId, "FLIGHT_ORDER")) {
            return null;
        }

        SystemNotice notice = new SystemNotice();
        notice.setUsername(username);
        notice.setType("FLIGHT_ORDER");
        notice.setOrderId(orderId);
        notice.setTitle("机票订购成功");
        notice.setEmoji("✈️");
        notice.setContent(String.format(
            "您预订的%s → %s机票已出票成功！航班号：%s，起飞时间：%s，金额：¥%.2f。请提前2小时到达机场！",
            fromCity != null ? fromCity : "出发地",
            toCity != null ? toCity : "目的地",
            flightNo != null ? flightNo : "待确认",
            departureTime != null ? departureTime : "待确认",
            price != null ? price : 0
        ));
        notice.setIsRead(false);

        return systemNoticeRepository.save(notice);
    }

    /**
     * 获取用户所有通知
     */
    public List<SystemNotice> getUserNotices(String username) {
        return systemNoticeRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    /**
     * 获取用户未读通知数量
     */
    public long getUnreadCount(String username) {
        return systemNoticeRepository.findByUsernameAndIsReadFalse(username).size();
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long noticeId) {
        systemNoticeRepository.markAsRead(noticeId);
    }

    /**
     * 批量标记为已读
     */
    @Transactional
    public void markAllAsRead(String username) {
        systemNoticeRepository.markAllAsRead(username);
    }
}