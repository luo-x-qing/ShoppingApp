package com.example.ordermanagement.service;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.repository.FlightOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class FlightOrderService {

    @Autowired
    private FlightOrderRepository flightOrderRepository;

    @Autowired
    private SystemNoticeService systemNoticeService;

    public FlightOrder createOrder(FlightOrder order) {
        if (order.getFlightId() == null) {
            order.setFlightId(null);
        }
        if (order.getStatus() == null) {
            order.setStatus("待支付");
        }
        if (order.getCreateTime() == null) {
            order.setCreateTime(LocalDateTime.now());
        }
        return flightOrderRepository.save(order);
    }

    public Optional<FlightOrder> getOrderById(Long id) {
        return flightOrderRepository.findById(id);
    }

    public List<FlightOrder> getOrdersByUsername(String username) {
        return flightOrderRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    public List<FlightOrder> getAllOrders() {
        return flightOrderRepository.findAll();
    }

    /**
     * 更新订单状态（支付/取消）
     */
    @Transactional
    public FlightOrder updateOrderStatus(Long id, String status) {
        Optional<FlightOrder> optional = flightOrderRepository.findById(id);
        if (optional.isPresent()) {
            FlightOrder order = optional.get();
            String oldStatus = order.getStatus();

            // 只有待支付状态才能取消
            if ("已取消".equals(status) && !"待支付".equals(oldStatus)) {
                throw new RuntimeException("只有待支付的订单才能取消");
            }

            order.setStatus(status);

            if ("已支付".equals(status)) {
                order.setPayTime(LocalDateTime.now());
                // 支付成功后创建通知
                createOrderNotice(order);
            } else if ("已取消".equals(status)) {
                order.setCancelTime(LocalDateTime.now());
            }

            return flightOrderRepository.save(order);
        }
        return null;
    }

    /**
     * 取消订单
     */
    @Transactional
    public FlightOrder cancelOrder(Long id) {
        Optional<FlightOrder> optional = flightOrderRepository.findById(id);
        if (optional.isPresent()) {
            FlightOrder order = optional.get();
            // 只有待支付状态才能取消
            if (!"待支付".equals(order.getStatus())) {
                throw new RuntimeException("只有待支付的订单才能取消");
            }
            order.setStatus("已取消");
            order.setCancelTime(LocalDateTime.now());
            return flightOrderRepository.save(order);
        }
        return null;
    }

    /**
     * 删除订单
     */
    public boolean deleteOrder(Long id) {
        if (flightOrderRepository.existsById(id)) {
            flightOrderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 创建机票订单通知
     * 当订单支付成功时自动调用
     */
    private void createOrderNotice(FlightOrder order) {
        try {
            // 格式化起飞时间
            String departTimeStr = "";
            if (order.getDepartTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                departTimeStr = order.getDepartTime().format(formatter);
            } else {
                departTimeStr = "待确认";
            }

            // 调用通知服务创建通知
            systemNoticeService.createFlightOrderNotice(
                    order.getUsername(),           // 用户名
                    order.getId(),                  // 订单ID
                    order.getDepartCity(),          // 出发城市
                    order.getArriveCity(),          // 到达城市
                    order.getFlightNumber(),        // 航班号
                    departTimeStr,                  // 起飞时间
                    order.getPrice()                // 价格
            );

            System.out.println("创建机票订单通知成功: orderId=" + order.getId() +
                    ", username=" + order.getUsername() +
                    ", 航班=" + order.getFlightNumber());
        } catch (Exception e) {
            System.err.println("创建机票订单通知失败: orderId=" + order.getId() +
                    ", error=" + e.getMessage());
            e.printStackTrace();
        }
    }
}