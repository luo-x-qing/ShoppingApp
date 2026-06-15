package com.example.ordermanagement.task;

import com.example.ordermanagement.repository.HotelOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@EnableScheduling
public class OrderExpireTask {

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    public void expireOrders() {
        int count = hotelOrderRepository.expireOrders(LocalDate.now());
        System.out.println("已过期订单数量：" + count);
    }
}
