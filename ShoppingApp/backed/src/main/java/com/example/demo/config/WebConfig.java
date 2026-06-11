package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /file/** 映射到你的图片存放文件夹
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:D:/你的图片文件夹路径/");
    }
}