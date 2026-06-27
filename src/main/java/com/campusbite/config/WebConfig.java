package com.campusbite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 * Purpose: Registers the uploads/food-images folder as a static resource location
 *          so uploaded images can be accessed via URLs like /uploads/food-images/abc.jpg.
 *          Without this, Spring Boot would not know to serve files from outside the classpath.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/food-images/** URL path to the actual folder on the filesystem
        registry.addResourceHandler("/uploads/food-images/**")
                .addResourceLocations("file:" + uploadDir + "/");

        // Keep existing static resources (images/food/, css/, js/) working
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
