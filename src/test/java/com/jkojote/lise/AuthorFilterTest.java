package com.jkojote.lise;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class AuthorFilterTest {

    @Autowired
    private EntityUrlParamsFilter<Author> authorFilter;

    @Test
    public void findAllTest() {
        /* it works but data may change later and result won't be correct
        String url = "http://example/authors?fn=Richard&mn=*&ln=dawkins";
        List<Author> authors = authorFilter.findAll(url);
        assertEquals(1, authors.size());
        assertEquals(Name.of("Richard", "Dawkins"), authors.get(0).getName());
        url = "http://example/authors?fn=erich&ln=remarq";
        authors = authorFilter.findAll(url);
        assertEquals(1, authors.size());
        assertEquals(Name.of("Erich", "Maria", "Remarque"), authors.get(0).getName());
        */
    }
}
