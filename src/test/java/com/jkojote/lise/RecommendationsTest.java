package com.jkojote.lise;

import com.jkojote.library.clauses.SqlClauseBuilder;
import com.jkojote.library.domain.model.reader.Reader;
import com.jkojote.library.domain.shared.domain.FilteringAndSortingRepository;
import com.jkojote.libraryserver.application.recomendations.Recommendation;
import com.jkojote.libraryserver.application.recomendations.RecommendationsGenerator;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class RecommendationsTest {

    @Autowired
    private SqlClauseBuilder clauseBuilder;

    @Autowired
    private FilteringAndSortingRepository<Reader> readerRepository;

    @Autowired
    private RecommendationsGenerator generator;

    @Test
    public void getFor() {
        Reader reader = readerRepository.findById(13);
        List<Recommendation> recommendations = generator.getFor(reader, 5);
        System.out.println(recommendations);
    }
}
