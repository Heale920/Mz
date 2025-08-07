package org.example.blogplarform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev") //  <- 关键点1：只在 dev 环境生效

public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println(" DEV 环境 CORS 配置已加载，允许所有 ngrok-free.app 来源。");

        registry.addMapping("/**")
                // 关键就在这里！
                // 这个模式会匹配你前端的任何一个 ngrok 动态地址
                .allowedOriginPatterns(
                        "https://*.ngrok-free.app",
                        "http://*.ngrok-free.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }


}

