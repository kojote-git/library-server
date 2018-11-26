package com.jkojote.libraryserver.application.controllers.utils.filters;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParser;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component("authorFilter")
public class AuthorFilter implements EntityUrlParamsFilter<Author> {

    private DomainRepository<Author> authorRepository;

    private QueryStringParser parametersParser;

    @Autowired
    public AuthorFilter(@Qualifier("authorRepository")
                        DomainRepository<Author> authorRepository) {

        this.authorRepository = authorRepository;
        this.parametersParser = new QueryStringParserImpl();
    }

    @Override
    public List<Author> findAll(String url) {
        Map<String, String> params = parametersParser.getParams(url);
        if (params.isEmpty()) {
            return authorRepository.findAll();
        }
        return find(params);
    }

    @Override
    public List<Author> findAllQueryString(String queryString) {
        Map<String, String> params = parametersParser.getParamsFromQueryString(queryString);
        if (params.isEmpty()) {
            return authorRepository.findAll();
        }
        return find(params);
    }

    private List<Author> find(Map<String, String> params) {
        Predicate<Author> predicate = a -> true;
        if (params.containsKey("fn")) {
            String firstName = params.get("fn");
            if (!firstName.contains("*")) {
                Pattern pattern = Pattern.compile("(?i)" + firstName);
                predicate = predicate.and(a -> pattern.matcher(a.getName().getFirstName()).find());
            }
        }
        if (params.containsKey("mn")) {
            String middleName = params.get("mn");
            if (!middleName.contains("*")) {
                Pattern pattern = Pattern.compile("(?i)" + middleName);
                predicate = predicate.and(a -> pattern.matcher(a.getName().getMiddleName()).find());
            }
        }
        if (params.containsKey("ln")) {
            String lastName = params.get("ln");
            if (!lastName.contains("*")) {
                Pattern pattern = Pattern.compile("(?i)" + lastName);
                predicate = predicate.and(a -> pattern.matcher(a.getName().getLastName()).find());
            }
        }
        return authorRepository.findAll(predicate);
    }
}
