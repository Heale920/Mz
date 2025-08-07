package org.example.blogplarform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 排除 API 路径
        registry.addViewController("/{spring:(?!api|favorite|article|user|comment|tag)[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:(?!api|favorite|article|user|comment|tag)[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:(?!api|favorite|article|user|comment|tag)[a-zA-Z0-9-_]+}/**{spring:?!(\\.js|\\.css|\\.png|\\.jpg)$}")
                .setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/avatar/**")
                .addResourceLocations("file:uploads/avatar/"); // 生产环境外部目录
    }
}
