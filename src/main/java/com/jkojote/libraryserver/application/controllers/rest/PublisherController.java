package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jkojote.libraryserver.application.controllers.Util.errorResponse;
import static com.jkojote.libraryserver.application.controllers.Util.responseMessage;

@RestController
@RequestMapping("/rest/publishers")
public class PublisherController {

    private DomainRepository<Publisher> publisherRepository;

    private DomainRepository<Book> bookRepository;

    private JsonConverter<Publisher> publisherJsonConverter;

    private JsonConverter<Book> bookJsonConverter;

    private JsonParser jsonParser;

    @Autowired
    public PublisherController(@Qualifier("publisherRepository")
                               DomainRepository<Publisher> publisherRepository,
                               @Qualifier("bookRepository")
                               DomainRepository<Book> bookRepository,
                               @Qualifier("publisherJsonConverter")
                               JsonConverter<Publisher> publisherJsonConverter,
                               @Qualifier("bookJsonConverter")
                               JsonConverter<Book> bookJsonConverter) {
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.jsonParser = new JsonParser();
        this.publisherJsonConverter = publisherJsonConverter;
        this.bookJsonConverter = bookJsonConverter;
    }

    @GetMapping("/")
    @CrossOrigin
    public ResponseEntity<String> getAll() {
        List<Publisher> publishers = publisherRepository.findAll();
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        obj.add("publishers", array);
        for (Publisher p : publishers) {
            array.add(publisherJsonConverter.convertToJson(p));
        }
        return new ResponseEntity<>(obj.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getPublisher(@PathVariable("id") long id) {
        Publisher publisher = publisherRepository.findById(id);
        if (publisher == null)
            return errorResponse("no such publisher with id " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        String json = publisherJsonConverter.convertToString(publisher);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @AuthorizationRequired
    @PostMapping("creation")
    public ResponseEntity<String> creation(ServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            String name = json.get("name").getAsString();
            long id = publisherRepository.nextId();
            Publisher publisher = new Publisher(id, name, new ArrayList<>());
            publisherRepository.save(publisher);
        }
        return responseMessage("publisher's been created", HttpStatus.CREATED);
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    public ResponseEntity<String> deleting(@PathVariable("id") long id) {
        Publisher p = publisherRepository.findById(id);
        if (p == null)
            return errorResponse("no such publisher with id "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        publisherRepository.remove(p);
        return responseMessage("publisher's been deleted", HttpStatus.OK);
    }


    @GetMapping("{id}/books")
    @CrossOrigin
    public ResponseEntity<String> getBooks(@PathVariable("id") long id) {
        Publisher p = publisherRepository.findById(id);
        if (p == null)
            return errorResponse("nu such publisher with id "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        List<Book> books = p.getBooks();
        JsonObject responseJson = new JsonObject();
        JsonArray booksArray = new JsonArray();
        for (Book b : books) {
            booksArray.add(bookJsonConverter.convertToJson(b));
        }
        responseJson.add("books", booksArray);
        return new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);
    }

    @AuthorizationRequired
    @PutMapping("{id}/editing")
    public ResponseEntity<String> editPublisher(@PathVariable("id") long id, ServletRequest req)
    throws IOException {
        Publisher publisher = publisherRepository.findById(id);
        if (publisher == null)
            return errorResponse("no such publisher with id " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        try (BufferedReader reader = req.getReader()) {
            JsonObject reqJson = jsonParser.parse(reader).getAsJsonObject();
            String name = reqJson.get("name").getAsString();
            publisher.setName(name);
            if (reqJson.has("books")) {
                JsonArray array = reqJson.get("books").getAsJsonArray();
                compareAndEditBooks(publisher, array);
            }
            publisherRepository.update(publisher);
            return responseMessage("publisher's been updated", HttpStatus.OK);
        }
    }

    //TODO implement this later
    private void compareAndEditBooks(Publisher publisher, JsonArray array) {
        for (JsonElement e : array) {
            JsonObject json = e.getAsJsonObject();
            long bookId = json.get("id").getAsLong();
            String action = json.get("action").getAsString();
        }
    }
}
