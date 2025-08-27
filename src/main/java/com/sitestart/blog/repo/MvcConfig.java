package com.sitestart.blog.repo;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 This class makes it possible to give files from the uploads folder and work with them.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     If the browser asks for /uploads/...,
     then this method will look for this file in the uploads/ folder on the disk.
     * @param registry collection of rules for obtaining statistical resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
