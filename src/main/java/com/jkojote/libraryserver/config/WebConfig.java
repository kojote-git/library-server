package com.jkojote.libraryserver.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@Import(MvcConfig.class)
public class WebConfig {

}

