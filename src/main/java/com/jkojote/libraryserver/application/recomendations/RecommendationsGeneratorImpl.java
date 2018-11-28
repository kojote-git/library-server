package com.jkojote.libraryserver.application.recomendations;

import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jkojote.libraryserver.application.recomendations.Recommendation.RecommendationBuilder;

@Component
public class RecommendationsGeneratorImpl implements RecommendationsGenerator {

    private DomainRepository<Book> bookRepository;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public RecommendationsGeneratorImpl(
            @Qualifier("bookRepository")
            DomainRepository<Book> bookRepository,
            JdbcTemplate jdbcTemplate) {
        this.bookRepository = bookRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Recommendation> random() {
        List<Book> allBooks = bookRepository.findAll(b -> b.getBookInstances().size() > 0);
        Collections.shuffle(allBooks);
        final String instancesUrl = WebConfig.URL + "rest/instances/";
        final String booksUrl = WebConfig.WEBLIB_URL + "books/";
        RecommendationBuilder recommendation = RecommendationBuilder.create(true);
        return allBooks.stream()
                .map(b ->
                    recommendation
                        .withBook(b)
                        .withReference(booksUrl + b.getId())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
