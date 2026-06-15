package com.example.demo.config;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 0 * * * ?")
    public void autoUnbanUsers() {
        try {
            int count = userService.autoUnbanExpiredUsers();
            if (count > 0) {
                System.out.println("【定时任务】自动解禁了 " + count + " 个用户");
            }
        } catch (Exception e) {
            System.err.println("【定时任务】自动解禁用户失败: " + e.getMessage());
        }
    }
}
