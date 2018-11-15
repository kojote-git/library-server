package com.jkojote.lise;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.RowMapper;

import javax.servlet.http.Cookie;

import static org.junit.Assert.assertNotNull;

public class ApplicationContextTest {

    @Test
    @SuppressWarnings("unchecked")
    public void createsContextWithoutUnexpectedExceptions() {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(MvcConfig.class);
        // get some beans
        DomainRepository<Author> authorRepository = ctx.getBean("authorRepository", DomainRepository.class);
        RowMapper<Author> authorMapper = ctx.getBean("authorMapper", RowMapper.class);

        assertNotNull(authorRepository);
        assertNotNull(authorMapper);
    }

}
