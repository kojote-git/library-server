package com.jkojote.libraryserver.config;

import com.jkojote.library.config.PersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PersistenceConfig.class)
public class WebConfig {
}
