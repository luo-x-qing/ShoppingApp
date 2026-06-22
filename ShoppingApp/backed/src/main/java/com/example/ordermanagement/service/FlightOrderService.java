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

    @Transactional
    public FlightOrder updateOrderStatus(Long id, String status) {
        Optional<FlightOrder> optional = flightOrderRepository.findById(id);
        if (optional.isPresent()) {
            FlightOrder order = optional.get();
            String oldStatus = order.getStatus();
            if ("已取消".equals(status) && !"待支付".equals(oldStatus)) {
                throw new RuntimeException("只有待支付的订单才能取消");
            }
            order.setStatus(status);
            if ("已支付".equals(status)) {
                order.setPayTime(LocalDateTime.now());
                createOrderNotice(order);
            } else if ("已取消".equals(status)) {
                order.setCancelTime(LocalDateTime.now());
            }
            return flightOrderRepository.save(order);
        }
        return null;
    }

    @Transactional
    public FlightOrder cancelOrder(Long id) {
        Optional<FlightOrder> optional = flightOrderRepository.findById(id);
        if (optional.isPresent()) {
            FlightOrder order = optional.get();
            if (!"待支付".equals(order.getStatus())) {
                throw new RuntimeException("只有待支付的订单才能取消");
            }
            order.setStatus("已取消");
            order.setCancelTime(LocalDateTime.now());
            return flightOrderRepository.save(order);
        }
        return null;
    }

    public boolean deleteOrder(Long id) {
        if (flightOrderRepository.existsById(id)) {
            flightOrderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void createOrderNotice(FlightOrder order) {
        try {
            String departTimeStr = "";
            if (order.getDepartTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                departTimeStr = order.getDepartTime().format(formatter);
            } else {
                departTimeStr = "待确认";
            }
            systemNoticeService.createFlightOrderNotice(
                    order.getUsername(),
                    order.getId(),
                    order.getDepartCity(),
                    order.getArriveCity(),
                    order.getFlightNumber(),
                    departTimeStr,
                    order.getPrice()
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
