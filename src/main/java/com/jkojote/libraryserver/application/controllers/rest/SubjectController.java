package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.work.Subject;
import com.jkojote.library.domain.model.work.SubjectTable;
import com.jkojote.libraryserver.application.controllers.adm.AdminController;
import com.jkojote.libraryserver.application.controllers.utils.ControllerUtils;
import com.jkojote.libraryserver.application.controllers.utils.Queries;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/rest/subj")
public class SubjectController {

    private QueryStringParser queryParser;

    private SubjectTable subjectTable;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public SubjectController(
            QueryStringParser queryStringParser,
            SubjectTable subjectTable,
            JdbcTemplate jdbcTemplate) {
        this.queryParser = queryStringParser;
        this.subjectTable = subjectTable;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("stats/{subj}")
    @CrossOrigin
    public ResponseEntity<String> getDownloadStats(
            @PathVariable("subj") String subj, HttpServletRequest req) {
        int id = subjectTable.exists(Subject.of(subj));
        if (id <= 0) {
            return ControllerUtils.errorResponse("no such subject exists", HttpStatus.NOT_FOUND);
        }
        Map<String, String> params = queryParser.getParamsFromQueryString(req.getQueryString());
        String dateBeginParam = params.getOrDefault("dateBegin", "1000-01-01");
        String dateEndParam = params.getOrDefault("dateEnd", "9999-01-01");
        LocalDate dateBegin, dateEnd;
        try {
            dateBegin = LocalDate.parse(dateBeginParam, DateTimeFormatter.ISO_LOCAL_DATE);
            dateEnd = LocalDate.parse(dateEndParam, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return ControllerUtils.errorResponse("invalid date format", HttpStatus.BAD_REQUEST);
        }
        Long totalDownloads = jdbcTemplate.queryForObject(Queries.SUBJECTS_STATISTICS,
                        new Object[]{subj, dateBegin, dateEnd}, Long.TYPE);
        JsonObject result = new JsonObject();
        result.add("id", new JsonPrimitive(id));
        result.add("subject", new JsonPrimitive(subj));
        result.add("totalDownloads", new JsonPrimitive(totalDownloads == null ? 0 : totalDownloads));
        result.add("dateBegin", new JsonPrimitive(dateBeginParam));
        result.add("dateEnd", new JsonPrimitive(dateEndParam));
        return ControllerUtils.responseEntityJson(result.toString(), HttpStatus.OK);
    }

    @GetMapping("form")
    public ModelAndView getForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        modelAndView.setViewName("subject/subject-stats");
        return modelAndView;
    }

}
