package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.Name;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

import static com.jkojote.libraryserver.application.controllers.Util.*;

@RestController
@RequestMapping("/rest/authors")
@SuppressWarnings("unchecked")
public class AuthorController {

    private DomainRepository<Author> authorRepository;

    private DomainRepository<Work> workRepository;

    private JsonConverter<Author> authorJsonConverter;

    private JsonConverter<Work> workJsonConverter;

    private JsonParser jsonParser;

    @Autowired
    public AuthorController(
            @Qualifier("authorRepository")
            DomainRepository<Author> authorRepository,
            @Qualifier("workRepository")
            DomainRepository<Work> workRepository,
            @Qualifier("authorJsonConverter")
            JsonConverter<Author> authorJsonConverter,
            @Qualifier("workJsonConverter")
            JsonConverter<Work> workJsonConverter) {
        this.authorRepository = authorRepository;
        this.workRepository = workRepository;
        this.jsonParser = new JsonParser();
        this.authorJsonConverter = authorJsonConverter;
        this.workJsonConverter = workJsonConverter;
    }

    @AuthorizationRequired
    @PostMapping("creation")
    public ResponseEntity<String> creation(HttpServletRequest req) throws IOException {
        long id = authorRepository.nextId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            json.add("id", new JsonPrimitive(id));
            String firstName = json.get("firstName").getAsString();
            String middleName = json.get("middleName").getAsString();
            String lastName = json.get("lastName").getAsString();
            Name name = Name.of(firstName, middleName, lastName);
            Author author = Author.createNew(id, name);
            authorRepository.save(author);
            return new ResponseEntity<>("{\"id\":"+id+"}", headers, HttpStatus.CREATED);
        }
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getAuthor(@PathVariable("id") long id) {
        Author author = authorRepository.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        if (author == null) {
            return errorResponse("no author with such id: " + id, headers, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(authorJsonConverter.convertToString(author),
                headers, HttpStatus.OK);
    }

    @GetMapping("{id}/works")
    @CrossOrigin
    public ResponseEntity<String> getWorks(@PathVariable("id") long id) {
        Author author = authorRepository.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        if (author == null) {
            return errorResponse("no author with such id: " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Work w : author.getWorks()) {
            array.add(workJsonConverter.convertToJson(w));
        }
        json.add("works", array);
        return new ResponseEntity<>(json.toString(), headers, HttpStatus.OK);
    }

    @AuthorizationRequired
    @PutMapping("{id}/editing")
    public ResponseEntity<String> editAuthor(@PathVariable("id") long id, HttpServletRequest req) {
        Author a = authorRepository.findById(id);
        if (a == null) {
            return errorResponse("no author with such id:" + id, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        try {
            JsonObject json;
            try (BufferedReader reader = req.getReader()) {
                json = jsonParser.parse(reader).getAsJsonObject();
            }
            if (json.has("works")) {
                compareAndEditWorks(a, json.get("works").getAsJsonArray());
            }
            String firstName = json.get("firstName").getAsString(),
                   middleName = json.get("middleName").getAsString(),
                   lastName  = json.get("lastName").getAsString();
            Name newName = Name.of(firstName, middleName, lastName);
            a.setName(newName);
            authorRepository.update(a);
            return responseMessage("author has been updated", HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    public ResponseEntity<String> deleteAuthor(@PathVariable("id") long id, HttpServletRequest req) {
        Author a = authorRepository.findById(id);
        if (a == null)
            return errorResponse("no such author with id " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        authorRepository.remove(a);
        return responseMessage("author's been deleted", HttpStatus.OK);
    }

    private void compareAndEditWorks(Author author, JsonArray works) {
        for (JsonElement e : works) {
            JsonObject o = e.getAsJsonObject();
            if (o.get("action").getAsString().equals("remove")) {
                author.removeWork(workRepository.findById(o.get("id").getAsLong()));
            } else {
                author.addWork(workRepository.findById(o.get("id").getAsLong()));
            }
        }
    }
}