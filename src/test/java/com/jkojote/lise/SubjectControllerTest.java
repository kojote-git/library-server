package com.jkojote.lise;

import com.jkojote.libraryserver.application.controllers.rest.SubjectController;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class SubjectControllerTest {

    @Autowired
    private SubjectController subjectController;

    @Test
    public void reportHtml() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getQueryString()).thenReturn("");
        ResponseEntity<String> res =
                subjectController.getDownloadStats("Новела", request);
    }
}
