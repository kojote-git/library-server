package com.jkojote.lise;

import com.jkojote.library.domain.model.work.Work;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class WorkFilterTest {

    @Autowired
    private EntityUrlParamsFilter<Work> workFilter;

    @Test
    public void getAll() {
        List<Work> works = workFilter.findAllQueryString("title=The%20God%20Delusion");
        assertEquals(1, works.size());
    }
}
