package com.jkojote.libraryserver.config;

import com.jkojote.library.clauses.SqlClauseBuilder;
import com.jkojote.library.clauses.mysql.MySqlClauseBuilder;
import com.jkojote.library.config.PersistenceConfig;
import com.jkojote.libraryserver.application.mailing.MailSender;
import com.jkojote.libraryserver.application.mailing.MailSenderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PersistenceConfig.class, ThymeleafConfig.class})
@ComponentScan("com.jkojote.libraryserver.application")
public class MvcConfig {

    @Bean
    public MailSender mailSender() {
        return new MailSenderImpl(true);
    }

    @Bean
    public SqlClauseBuilder builder() {
        return new MySqlClauseBuilder();
    }

}
