package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/work")
public class WorkController {

    private DomainRepository<Work> workRepository;

    private DomainRepository<Author> authorRepository;

    private HttpHeaders defaultHeaders;

    private JsonParser jsonParser;

    @Autowired
    public WorkController(@Qualifier("workRepository")
                          DomainRepository<Work> workRepository,
                          @Qualifier("authorRepository")
                          DomainRepository<Author> authorRepository) {
        this.authorRepository = authorRepository;
        this.workRepository = workRepository;
        this.jsonParser = new JsonParser();
        this.defaultHeaders = new HttpHeaders();
        defaultHeaders.set("Content-Type", "application/json");
        defaultHeaders.add("Access-Control-Allow-Origin", "*");
    }

    @PostMapping("creation")
    public ResponseEntity<String> createWork(ServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            long authorId = json.get("author").getAsLong();
            Author author = authorRepository.findById(authorId);
            if (author == null) {
                return responseError("author doesn't exist", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            long workId = workRepository.nextId();
            String title = json.get("title").getAsString();
            Work work = Work.create(workId, title, author);
            workRepository.save(work);
            return new ResponseEntity<>("{\"id\":\""+workId+"\"}", defaultHeaders, HttpStatus.CREATED);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<String> getWork(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return responseError("work with specified id doesn't exist yet", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(work.getId()));
        json.add("title", new JsonPrimitive(work.getTitle()));
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    @GetMapping("{id}/description")
    public ResponseEntity<String> getDescription(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return responseError("work with specified id doesn't exist yet", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(id));
        json.add("title", new JsonPrimitive(work.getTitle()));
        json.add("description", new JsonPrimitive(work.getDescription().toString()));
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    private ResponseEntity<String> responseError(String message, HttpStatus status) {
        JsonObject json = new JsonObject();
        json.add("error", new JsonPrimitive(message));
        return new ResponseEntity<>(json.toString(), defaultHeaders, status);
    }

}
