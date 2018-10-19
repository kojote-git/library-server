package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.Name;
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
@RequestMapping("/author")
@SuppressWarnings("unchecked")
public class AuthorController {

    private DomainRepository<Author> authorRepository;

    private JsonParser jsonParser;

    @Autowired
    public AuthorController(
            @Qualifier("authorRepository")
            DomainRepository<Author> authorRepository) {
        this.authorRepository = authorRepository;
        this.jsonParser = new JsonParser();
    }

    @PostMapping("creation")
    private ResponseEntity<String> creation(ServletRequest req) throws IOException {
        long id = authorRepository.nextId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Access-Control-Allow-Origin", "*");
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            String firstName = json.get("firstName").getAsString();
            String middleName = json.get("middleName").getAsString();
            String lastName = json.get("lastName").getAsString();
            Name name = Name.of(firstName, middleName, lastName);

            Author author = Author.createNew(id, name);
            authorRepository.save(author);
            return new ResponseEntity<>("{\"id\":\"" + id + "\"}", headers, HttpStatus.CREATED);
        }
    }

    @GetMapping("{id}")
    private ResponseEntity<String> getAuthor(@PathVariable("id") long id) {
        Author author = authorRepository.findById(id);
        JsonObject jsonObject = new JsonObject();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Access-Control-Allow-Origin", "*");
        if (author == null) {
            return new ResponseEntity<>("{\"error\":\"author doesn't exist\"}", headers, HttpStatus.OK);
        }
        Name name = author.getName();
        jsonObject.add("firstName", new JsonPrimitive(name.getFirstName()));
        jsonObject.add("middleName", new JsonPrimitive(name.getMiddleName()));
        jsonObject.add("lastName", new JsonPrimitive(name.getLastName()));
        return new ResponseEntity<>(jsonObject.toString(), headers, HttpStatus.OK);
    }
}
