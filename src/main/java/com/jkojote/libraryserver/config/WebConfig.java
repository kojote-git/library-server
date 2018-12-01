package com.jkojote.libraryserver.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Import(MvcConfig.class)
public class WebConfig implements WebMvcConfigurer {

    public static final String URL = "http://localhost:8080/lise/";

    public static final String WEBLIB_URL = "http://localhost:8080/weblib/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/res/**").addResourceLocations("/res/");
    }
}

