package com.example.suco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục gốc của dự án
        String rootDir = System.getProperty("user.dir");
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + rootDir + File.separator + "uploads" + File.separator);
        
        System.out.println("Upload Path: file:" + rootDir + File.separator + "uploads" + File.separator);
    }
}