package com.jkojote.lise;

import com.jkojote.libraryserver.application.QueryRunner;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class QueryRunnerTest {

    @Autowired
    private QueryRunner runner;

    @Test
    public void runQuery() {
        String query = "SELECT id, title FROM Work";
        System.out.println(runner.runQuery(query));
        query = "SELECT " +
                  "work.id, work.title, author.id, author.firstName, author.lastName " +
                "FROM Work work " +
                "INNER JOIN WorkAuthor workAuthor ON work.id = workAuthor.workId " +
                "INNER JOIN Author author ON workAuthor.authorId = author.id";
        System.out.println(runner.runQuery(query));
    }
}
