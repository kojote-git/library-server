package com.jkojote.libraryserver.application.recomendations;

import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.library.domain.model.reader.Download;
import com.jkojote.library.domain.model.reader.Rating;
import com.jkojote.library.domain.model.reader.Reader;
import com.jkojote.library.domain.model.work.Subject;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.utils.Queries;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static com.jkojote.libraryserver.application.recomendations.Recommendation.RecommendationBuilder;
import static java.util.stream.Collectors.*;

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
    public List<Recommendation> getFor(Reader reader, int size) {
        List<Recommendation> recommendations = getFor(reader);
        if (recommendations.size() < size) {
            List<Recommendation> random = random();
            random.forEach(r -> {
                        if (!recommendations.contains(r))
                            recommendations.add(r);
                    });
        }
        return recommendations.subList(0, size);
    }

    @Override
    public List<Recommendation> getFor(Reader reader) {
        if (reader.getDownloads().size() == 0 || reader.getRatings().size() == 0)
            return random();
        RecommendationBuilder recommendation = RecommendationBuilder.create(true);
        // get books that haven't been rated or downloaded yet
        List<Book> notDownloaded = getNotDownloaded(reader);
        List<Book> notRated = getNotRated(reader);
        List<Book> toRecommend = new ArrayList<>(notDownloaded);
        toRecommend.addAll(notRated);
        toRecommend = toRecommend.stream().distinct().collect(toList());
        if (toRecommend.size() == 0)
            return random();
//        List<Book> toRecommend = bookRepository.findAll();
        // get coefficients for subject based on downloads and ratings
        Map<Subject, Double> coefficients =
                getSubjectsCoefficients(getDownloadedSubjects(reader), getRatedSubjects(reader));
        // get all books their ratings
        List<Map.Entry<Book, BigDecimal>> bookRatings =
                new ArrayList<>(getBooksRatings().entrySet());
        // get recommendation coefficient for each book
        Map<Book, Double> recommendationsCoefficients = getRecommended(bookRatings, coefficients);
        for (ListIterator<Book> i = toRecommend.listIterator(); i.hasNext(); ) {
            Book b = i.next();
            double coeff = recommendationsCoefficients.get(b);
            if (coeff < 4)
                i.remove();
        }
        List<Recommendation> recommendations = toRecommend.stream()
                .map(b ->
                    recommendation
                        .withBook(b)
                        .withReference(WebConfig.WEBLIB_URL + "books/" + b.getId())
                        .build())
                .collect(toList());
        Collections.shuffle(recommendations);
        return recommendations;
    }

    @Override
    public List<Recommendation> random() {
        List<Book> allBooks = bookRepository.findAll(b -> b.getBookInstances().size() > 0);
        Collections.shuffle(allBooks);
        final String booksUrl = WebConfig.WEBLIB_URL + "books/";
        RecommendationBuilder recommendation = RecommendationBuilder.create(true);
        return allBooks.stream()
                .map(b ->
                    recommendation
                        .withBook(b)
                        .withReference(booksUrl + b.getId())
                        .build()
                )
                .collect(toList());
    }

    private List<Book> getNotDownloaded(Reader reader) {
        return bookRepository.findAll(
                    b -> reader.getDownloads().stream()
                            .map(Download::getInstance)
                            .map(BookInstance::getBook)
                            .distinct()
                            .noneMatch(b1 -> b1.equals(b)));
    }

    private List<Book> getNotRated(Reader reader) {
        return bookRepository.findAll(
                    b -> reader.getRatings().stream()
                            .map(Rating::getBook)
                            .noneMatch(b1 -> b1.equals(b)));
    }

    private Map<Subject, BigDecimal> getRatedSubjects(Reader reader) {
        Map<Subject, BigDecimal> result = new HashMap<>();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(Queries.RATED_SUBJECTS, reader.getId());
        while (rowSet.next()) {
            result.put(Subject.of(rowSet.getString("subject")),
                    rowSet.getBigDecimal("averageRating"));
        }
        return result;
    }

    private Map<Subject, Long> getDownloadedSubjects(Reader reader) {
        return reader.getDownloads().stream()
                .flatMap(d -> d.getInstance().getBook().getBasedOn().getSubjects().stream())
                .collect(groupingBy(Function.identity(), counting()));
    }

    private Map<Book, BigDecimal> getBooksRatings() {
        Map<Book, BigDecimal> result = new HashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(
                "SELECT id, AVG(rating) AS averageRating FROM Rating r " +
                "RIGHT JOIN Book b ON b.id = r.bookId GROUP BY id");
        while (rs.next()) {
            result.put(
                bookRepository.findById(rs.getLong("id")),
                rs.getBigDecimal("averageRating")
            );
        }
        return result;
    }

    private Map<Subject, Double>
    getSubjectsCoefficients(Map<Subject, Long> downloaded, Map<Subject, BigDecimal> rated) {
        Map<Subject, Double> res = new HashMap<>();
        List<Subject> all = new ArrayList<>(downloaded.keySet());
        all.addAll(rated.keySet());
        all = all.stream().distinct().collect(toList());
        for (Subject s : all) {
            double dCoeff = getDownloadCoefficient(downloaded.get(s));
            BigDecimal rating = rated.get(s);
            if (rating == null)
                res.put(s, dCoeff * 6);
            else
                res.put(s, dCoeff * rating.doubleValue());
        }
        return res;
    }

    private Map<Book, Double> getRecommended(List<Map.Entry<Book, BigDecimal>> all,
                                             Map<Subject, Double> coefficients) {
        Map<Book, Double> res = new HashMap<>();
        for (Map.Entry<Book, BigDecimal> bookEntry : all) {
            Book book = bookEntry.getKey();
            BigDecimal rating = bookEntry.getValue();
            rating = rating == null ? BigDecimal.valueOf(8) : rating;
            List<Subject> subjects = book.getBasedOn().getSubjects();
            if (subjects.size() != 0) {
                double coef = 0;
                for (Subject s : subjects)
                    coef += coefficients.getOrDefault(s, 0.2d);
                coef /= subjects.size();
                coef = coef * rating.doubleValue();
                res.put(book, coef);
            } else {
                res.put(book, rating.doubleValue() * 0.85);
            }
        }
        return res;
    }

    private double getDownloadCoefficient(Long downloadCount) {
        if (downloadCount == null || downloadCount.compareTo(3L) < 0)
            return 0.1;
        if (downloadCount.compareTo(30L) >= 0)
            return 1;
        return 0.1 * downloadCount / 3f;
    }
}
