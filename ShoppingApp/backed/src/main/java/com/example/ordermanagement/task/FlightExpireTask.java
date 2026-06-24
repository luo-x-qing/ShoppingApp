package com.example.ordermanagement.task;

import com.example.ordermanagement.repository.FlightOrderRepository;
import com.example.ordermanagement.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@EnableScheduling
public class FlightExpireTask {

    private static final Logger log = LoggerFactory.getLogger(FlightExpireTask.class);

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightOrderRepository flightOrderRepository;

    // 每天凌晨2点执行：处理过期航班
    @Scheduled(cron = "0 0 2 * * ?")
    public void expireFlights() {
        log.info("开始执行过期航班处理任务，时间：{}", LocalDateTime.now());
        try {
            int expiredCount = flightRepository.expireFlights(LocalDateTime.now());
            log.info("已过期航班数量：{}", expiredCount);
        } catch (Exception e) {
            log.error("处理过期航班失败：", e);
        }
    }

    // 每小时执行一次：处理未支付的过期订单（超过30分钟未支付）
    @Scheduled(cron = "0 0/30 * * * ?")
    public void expireOrders() {
        log.info("开始执行过期订单处理任务，时间：{}", LocalDateTime.now());
        try {
            // 30分钟前的订单视为过期
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
            int expiredCount = flightOrderRepository.expireOrders(expireTime);
            log.info("已过期订单数量：{}", expiredCount);
        } catch (Exception e) {
            log.error("处理过期订单失败：", e);
        }
    }

    // 每天凌晨3点执行：处理已完成订单（航班已到达）
    @Scheduled(cron = "0 0 3 * * ?")
    public void completeOrders() {
        log.info("开始执行订单完成处理任务，时间：{}", LocalDateTime.now());
        try {
            int completedCount = flightOrderRepository.completeOrders(LocalDateTime.now());
            log.info("已完成订单数量：{}", completedCount);
        } catch (Exception e) {
            log.error("处理已完成订单失败：", e);
        }
    }
}