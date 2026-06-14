package com.example.ordermanagement.service;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.repository.HotelOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HotelOrderService {

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    public List<HotelOrder> getAll() {
        return hotelOrderRepository.findAllByOrderByIdDesc();
    }

    public List<HotelOrder> getOrdersByUsername(String username) {
        return hotelOrderRepository.findByUsernameOrderByIdDesc(username);
    }

    public HotelOrder getById(Long id) {
        Optional<HotelOrder> opt = hotelOrderRepository.findById(id);
        return opt.orElse(null);
    }

    public HotelOrder save(HotelOrder order) {
        return hotelOrderRepository.save(order);
    }

    public List<HotelOrder> getOrdersByMerchant(Long merchantId) {
        return hotelOrderRepository.findByMerchantId(merchantId);
    }

    public List<HotelOrder> getOrdersByHotelIds(List<Long> hotelIds) {
        return hotelOrderRepository.findByHotelIds(hotelIds);
    }

    public boolean confirmOrder(Long id) {
        return hotelOrderRepository.confirmOrder(id, LocalDateTime.now()) > 0;
    }

    public boolean approveCancel(Long id) {
        return hotelOrderRepository.cancelOrder(id, "已取消", "商家同意取消", LocalDateTime.now()) > 0;
    }

    public boolean rejectCancel(Long id) {
        return hotelOrderRepository.cancelOrder(id, "已确认", "商家拒绝取消", LocalDateTime.now()) > 0;
    }

    public void expireOrders() {
        hotelOrderRepository.expireOrders(LocalDate.now());
    }
}