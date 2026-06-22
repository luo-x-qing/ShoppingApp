package com.example.ordermanagement.service;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.HotelOrderRepository;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class HotelOrderService {

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private SystemNoticeService systemNoticeService;

    public List<HotelOrder> getAll() {
        return hotelOrderRepository.findAllByOrderByIdDesc();
    }

    public List<HotelOrder> getOrdersByUsername(String username) {
        return hotelOrderRepository.findByUsernameOrderByIdDesc(username);
    }

    public HotelOrder getById(Long id) {
        if (id == null) {
            return null;
        }
        return hotelOrderRepository.findById(id).orElse(null);
    }

    @Transactional
    public HotelOrder createOrder(HotelOrder order) {
        if (order.getRoomTypeId() == null) {
            throw new RuntimeException("房间类型不能为空");
        }

        RoomType roomType = roomTypeRepository.findById(order.getRoomTypeId()).orElse(null);
        if (roomType == null) {
            throw new RuntimeException("房间类型不存在");
        }
        if (roomType.getAvailableCount() < order.getRoomCount()) {
            throw new RuntimeException("房间库存不足，当前可用：" + roomType.getAvailableCount());
        }

        int updated = roomTypeRepository.decreaseAvailableCount(order.getRoomTypeId(), order.getRoomCount());
        if (updated == 0) {
            throw new RuntimeException("扣减库存失败，请重试");
        }

        if (order.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(order.getHotelId()).orElse(null);
            if (hotel != null && hotel.getMerchantId() != null) {
                order.setMerchantId(hotel.getMerchantId());
                System.out.println("设置订单商家ID: " + hotel.getMerchantId() + " (酒店: " + hotel.getName() + ")");
            }
        }

        if (order.getStatus() == null) {
            order.setStatus("待支付");
        }
        if (order.getCreateTime() == null) {
            order.setCreateTime(LocalDateTime.now());
        }

        return hotelOrderRepository.save(order);
    }

    @Transactional
    public boolean payOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"待支付".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("待确认");
        hotelOrderRepository.save(order);

        createOrderNotice(order);

        return true;
    }

    @Transactional
    public boolean confirmOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"待确认".equals(order.getStatus()) && !"已支付".equals(order.getStatus())) {
            return false;
        }
        String oldStatus = order.getStatus();
        order.setStatus("已确认");
        order.setConfirmTime(LocalDateTime.now());
        hotelOrderRepository.save(order);

        if ("待确认".equals(oldStatus)) {
            createOrderNotice(order);
        }

        return true;
    }

    @Transactional
    public boolean confirmCheckIn(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"已确认".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("已入住");
        hotelOrderRepository.save(order);
        return true;
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"待支付".equals(order.getStatus())) {
            return false;
        }
        order.setStatus("已取消");
        order.setCancelTime(LocalDateTime.now());
        hotelOrderRepository.save(order);

        if (order.getRoomTypeId() != null && order.getRoomCount() != null) {
            roomTypeRepository.increaseAvailableCount(order.getRoomTypeId(), order.getRoomCount());
        }
        return true;
    }

    @Transactional
    public boolean cancelOrderRequest(Long orderId, String reason, String username) {
        if (orderId == null) {
            System.out.println("取消申请失败: orderId为空");
            return false;
        }

        HotelOrder order = getById(orderId);
        if (order == null) {
            System.out.println("取消申请失败: 订单不存在, orderId=" + orderId);
            return false;
        }

        if (!order.getUsername().equals(username)) {
            System.out.println("取消申请失败: 用户不匹配, 订单用户=" + order.getUsername() + ", 当前用户=" + username);
            return false;
        }

        String status = order.getStatus();
        if (!"已支付".equals(status) && !"待确认".equals(status) && !"已确认".equals(status)) {
            System.out.println("取消申请失败: 订单状态不允许取消, status=" + status);
            return false;
        }

        if ("已入住".equals(status) || "已完成".equals(status)) {
            System.out.println("取消申请失败: 订单已入住或已完成, 不能取消");
            return false;
        }

        order.setStatus("取消申请中");
        order.setCancelReason(reason != null ? reason : "用户申请取消");
        hotelOrderRepository.save(order);

        System.out.println("取消申请成功: orderId=" + orderId + ", username=" + username + ", reason=" + reason);
        return true;
    }

    @Transactional
    public boolean approveCancel(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"取消申请中".equals(order.getStatus())) {
            System.out.println("同意取消失败: 订单状态不是取消申请中, status=" + (order != null ? order.getStatus() : "null"));
            return false;
        }

        order.setStatus("已取消");
        order.setCancelTime(LocalDateTime.now());
        hotelOrderRepository.save(order);

        if (order.getRoomTypeId() != null && order.getRoomCount() != null) {
            roomTypeRepository.increaseAvailableCount(order.getRoomTypeId(), order.getRoomCount());
        }

        System.out.println("同意取消成功: orderId=" + orderId);
        return true;
    }

    @Transactional
    public boolean rejectCancel(Long orderId, String reason) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"取消申请中".equals(order.getStatus())) {
            System.out.println("拒绝取消失败: 订单状态不是取消申请中");
            return false;
        }

        order.setStatus("已支付");
        order.setCancelReason("拒绝取消：" + (reason != null ? reason : "商家拒绝了取消申请"));
        hotelOrderRepository.save(order);

        System.out.println("拒绝取消成功: orderId=" + orderId + ", reason=" + reason);
        return true;
    }

    public List<HotelOrder> getOrdersByMerchant(List<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return Collections.emptyList();
        }
        return hotelOrderRepository.findByHotelIds(hotelIds);
    }

    public HotelOrder addComment(Long orderId, String comment, Integer rating) {
        if (orderId == null) {
            return null;
        }
        HotelOrder order = getById(orderId);
        if (order == null) return null;
        order.setComment(comment);
        order.setRating(rating);
        order.setStatus("已完成");
        return hotelOrderRepository.save(order);
    }

    public HotelOrder save(HotelOrder order) {
        return hotelOrderRepository.save(order);
    }

    public List<HotelOrder> getOrdersByMerchantId(Long merchantId) {
        if (merchantId == null) {
            return Collections.emptyList();
        }
        return hotelOrderRepository.findByMerchantId(merchantId);
    }

    @Transactional
    public boolean confirmCheckOut(Long orderId) {
        if (orderId == null) {
            return false;
        }
        HotelOrder order = getById(orderId);
        if (order == null || !"已入住".equals(order.getStatus())) {
            System.out.println("确认退房失败: 订单状态不是已入住");
            return false;
        }
        order.setStatus("已完成");
        hotelOrderRepository.save(order);
        System.out.println("确认退房成功: orderId=" + orderId);
        return true;
    }

    public void expireOrders() {
        hotelOrderRepository.expireOrders(LocalDate.now());
    }

    private void createOrderNotice(HotelOrder order) {
        try {
            String hotelName = order.getName();
            if (hotelName == null && order.getHotelId() != null) {
                Hotel hotel = hotelRepository.findById(order.getHotelId()).orElse(null);
                hotelName = hotel != null ? hotel.getName() : "酒店";
            }

            systemNoticeService.createHotelOrderNotice(order, hotelName != null ? hotelName : "酒店");
            System.out.println("创建订单通知成功: orderId=" + order.getId() + ", username=" + order.getUsername());
        } catch (Exception e) {
            System.err.println("创建订单通知失败: orderId=" + order.getId() + ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }

    public long countAll() {
        return hotelOrderRepository.count();
    }

    public long countByStatus(String status) {
        List<HotelOrder> orders = hotelOrderRepository.findByStatus(status);
        return orders.size();
    }
}
