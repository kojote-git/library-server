package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.exceptions.MalformedRequestException;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.errorResponse;
import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.responseEntityJson;
import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.responseMessage;

@RestController
@RequestMapping("/rest/publishers")
public class PublisherController {

    private DomainRepository<Publisher> publisherRepository;

    private JsonConverter<Publisher> publisherJsonConverter;

    private JsonConverter<Book> bookJsonConverter;

    private JsonParser jsonParser;

    @Autowired
    public PublisherController(@Qualifier("publisherRepository")
                               DomainRepository<Publisher> publisherRepository,
                               @Qualifier("publisherJsonConverter")
                               JsonConverter<Publisher> publisherJsonConverter,
                               @Qualifier("bookJsonConverter")
                               JsonConverter<Book> bookJsonConverter) {
        this.publisherRepository = publisherRepository;
        this.jsonParser = new JsonParser();
        this.publisherJsonConverter = publisherJsonConverter;
        this.bookJsonConverter = bookJsonConverter;
    }

    @GetMapping("")
    @CrossOrigin
    public ResponseEntity<String> getAll() {
        List<Publisher> publishers = publisherRepository.findAll();
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        obj.add("publishers", array);
        for (Publisher p : publishers) {
            array.add(publisherJsonConverter.convertToJson(p));
        }
        return responseEntityJson(obj.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getPublisher(@PathVariable("id") long id) {
        Publisher publisher = publisherRepository.findById(id);
        if (publisher == null)
            return errorResponse("no such publisherEditing with id " + id, HttpStatus.NOT_FOUND);
        String json = publisherJsonConverter.convertToString(publisher);
        return responseEntityJson(json, HttpStatus.OK);
    }

    @PostMapping("creation")
    @AuthorizationRequired
    public ResponseEntity<String> creation(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            validateCreationRequestBody(json);
            String name = json.get("name").getAsString();
            long id = publisherRepository.nextId();
            Publisher publisher = new Publisher(id, name, new ArrayList<>());
            publisherRepository.save(publisher);
            return responseEntityJson("{\"id\":"+id+"}", HttpStatus.CREATED);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @DeleteMapping("{id}/deleting")
    @AuthorizationRequired
    public ResponseEntity<String> deleting(HttpServletRequest req, @PathVariable("id") long id) {
        Publisher p = publisherRepository.findById(id);
        if (p == null)
            return errorResponse("no such publisherEditing with id "+id, HttpStatus.NOT_FOUND);
        publisherRepository.remove(p);
        return responseMessage("publisherEditing's been deleted", HttpStatus.OK);
    }


    @GetMapping("{id}/books")
    @CrossOrigin
    public ResponseEntity<String> getBooks(@PathVariable("id") long id) {
        Publisher p = publisherRepository.findById(id);
        if (p == null)
            return errorResponse("nu such publisherEditing with id "+id, HttpStatus.NOT_FOUND);
        List<Book> books = p.getBooks();
        JsonObject responseJson = new JsonObject();
        JsonArray booksArray = new JsonArray();
        for (Book b : books) {
            booksArray.add(bookJsonConverter.convertToJson(b));
        }
        responseJson.add("books", booksArray);
        return responseEntityJson(responseJson.toString(), HttpStatus.OK);
    }

    @PutMapping("{id}/editing")
    @AuthorizationRequired
    public ResponseEntity<String> editPublisher(@PathVariable("id") long id, HttpServletRequest req)
    throws IOException {
        Publisher publisher = publisherRepository.findById(id);
        if (publisher == null)
            return errorResponse("no such publisherEditing with id " + id, HttpStatus.NOT_FOUND);
        try (BufferedReader reader = req.getReader()) {
            JsonObject reqJson = jsonParser.parse(reader).getAsJsonObject();
            validateEditingRequestBody(reqJson);
            String name = reqJson.get("name").getAsString();
            publisher.setName(name);
            publisherRepository.update(publisher);
            return responseMessage("publisherEditing's been updated", HttpStatus.OK);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCreationRequestBody(JsonObject json) {
        if (!json.has("name"))
            throw new MalformedRequestException();
    }

    private void validateEditingRequestBody(JsonObject json) {
        validateCreationRequestBody(json);
    }
}
