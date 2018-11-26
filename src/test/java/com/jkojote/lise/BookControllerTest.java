package com.jkojote.lise;

import com.jkojote.libraryserver.application.controllers.rest.BookController;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class BookControllerTest {

    @Autowired
    private BookController bookController;

    @Test
    public void reportHtml() {
    }
}
