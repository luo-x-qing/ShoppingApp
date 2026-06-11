package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
/*    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://172.26.242.128:52939   ")); // 允许的源
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); // 允许的方法
        corsConfiguration.setAllowCredentials(true); // 是否允许发送Cookie
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); // 允许的头部

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}*/

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（开发环境用这个）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许携带 Cookie
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}