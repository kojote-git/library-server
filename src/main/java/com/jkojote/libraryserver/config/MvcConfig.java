package com.jkojote.libraryserver.config;

import com.jkojote.library.config.PersistenceConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PersistenceConfig.class)
@ComponentScan("com.jkojote.libraryserver.application")
public class MvcConfig {


}
