package com.jkojote.libraryserver.application.controllers.utils.filters;

import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component("workFilter")
public class WorkFilter implements EntityUrlParamsFilter<Work> {

    private DomainRepository<Work> workRepository;

    private QueryStringParser parametersParser;

    @Autowired
    public WorkFilter(@Qualifier("workRepository")
                      DomainRepository<Work> workRepository,
                      QueryStringParser parametersParser) {
        this.workRepository = workRepository;
        this.parametersParser = parametersParser;
    }

    @Override
    public List<Work> findAll(String url) {
        Map<String, String> params = parametersParser.getParams(url);
        if (params.isEmpty())
            return workRepository.findAll();
        return find(params);
    }

    @Override
    public List<Work> findAllQueryString(String queryString) {
        Map<String, String> params = parametersParser.getParamsFromQueryString(queryString);
        if (params.isEmpty())
            return workRepository.findAll();
        return find(params);
    }

    private List<Work> find(Map<String, String> params) {
        Predicate<Work> predicate = w -> true;
        if (params.containsKey("title")) {
            String title = params.get("title");
            if (!title.equals("*")) {
                Pattern pattern = Pattern.compile("(?i)" + title);
                predicate = predicate.and(w -> pattern.matcher(w.getTitle()).find());
            }
        }
        return workRepository.findAll(predicate);
    }
}
