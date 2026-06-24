package com.example.demo.config;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling  // 启用定时任务
public class ScheduledTasks {

    @Autowired
    private UserService userService;

    /**
     * 每小时执行一次，检查并自动解禁过期的用户
     * cron表达式：秒 分 时 日 月 周
     * 0 0 * * * ? 表示每小时的第0分钟第0秒执行
     */
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