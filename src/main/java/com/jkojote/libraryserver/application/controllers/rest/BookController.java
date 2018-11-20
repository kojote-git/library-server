package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.model.work.Work;
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

import static com.jkojote.libraryserver.application.controllers.utils.Util.errorResponse;
import static com.jkojote.libraryserver.application.controllers.utils.Util.responseEntityJson;
import static com.jkojote.libraryserver.application.controllers.utils.Util.responseMessage;

@RestController
@RequestMapping("/rest/books")
public class BookController {

    private JsonParser jsonParser;

    private DomainRepository<Book> bookRepository;

    private DomainRepository<Publisher> publisherRepository;

    private DomainRepository<Work> workRepository;

    private JsonConverter<Book> bookJsonConverter;

    private JsonConverter<BookInstance> bookInstanceJsonConverter;

    @Autowired
    public BookController(@Qualifier("bookRepository")
                          DomainRepository<Book> bookRepository,
                          @Qualifier("publisherRepository")
                          DomainRepository<Publisher> publisherRepository,
                          @Qualifier("workRepository")
                          DomainRepository<Work> workRepository,
                          @Qualifier("bookJsonConverter")
                          JsonConverter<Book> bookJsonConverter,
                          @Qualifier("biJsonConverter")
                          JsonConverter<BookInstance> bookInstanceJsonConverter) {
        this.bookRepository = bookRepository;
        this.jsonParser = new JsonParser();
        this.workRepository = workRepository;
        this.publisherRepository = publisherRepository;
        this.bookJsonConverter = bookJsonConverter;
        this.bookInstanceJsonConverter = bookInstanceJsonConverter;
    }

    @GetMapping("")
    @CrossOrigin
    public ResponseEntity<String> getAll() {
        List<Book> books = bookRepository.findAll();
        JsonArray res = new JsonArray();
        for (Book b : books) {
            res.add(bookJsonConverter.convertToJson(b));
        }
        return responseEntityJson(res.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getBook(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null) {
            return errorResponse("no such book with id: " + id, HttpStatus.NOT_FOUND);
        }
        return responseEntityJson(bookJsonConverter.convertToString(book), HttpStatus.OK);
    }

    @GetMapping("{id}/instances")
    @CrossOrigin
    public ResponseEntity<String> getBookInstances(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null) {
            return errorResponse("no such book with id: " + id, HttpStatus.NOT_FOUND);
        }
        List<BookInstance> instances = book.getBookInstances();
        JsonObject response = new JsonObject();
        JsonArray array = new JsonArray();
        for (BookInstance bi : instances) {
            array.add(bookInstanceJsonConverter.convertToJson(bi));
        }
        response.add("instances", array);
        return responseEntityJson(response.toString(), HttpStatus.OK);
    }

    @AuthorizationRequired
    @PostMapping("creation")
    @CrossOrigin
    public ResponseEntity<String> creation(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            validateCreationRequestBody(json);
            long publisherId = json.get("publisherId").getAsLong();
            long workId = json.get("workId").getAsLong();
            int edition = json.get("edition").getAsInt();
            Publisher publisher = publisherRepository.findById(publisherId);
            if (publisher == null)
                return errorResponse("no such publisherEditing with id: "+publisherId, HttpStatus.NOT_FOUND);
            Work work = workRepository.findById(workId);
            if (work == null)
                return errorResponse("no such work with id: "+workId, HttpStatus.NOT_FOUND);
            long id = bookRepository.nextId();
            Book book = new Book(id, work, publisher, edition, new ArrayList<>());
            bookRepository.save(book);
            return responseEntityJson("{\"id\":"+id+"}", HttpStatus.CREATED);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @AuthorizationRequired
    @PutMapping("{id}/editing")
    @CrossOrigin
    public ResponseEntity<String> editing(@PathVariable("id") long id, HttpServletRequest req)
    throws IOException {
        Book book = bookRepository.findById(id);
        if (book == null)
            return errorResponse("no such book with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            validateEditingRequestBody(json);
            int edition = json.get("edition").getAsInt();
            long publisherId = json.get("publisherId").getAsLong();
            Publisher publisher = publisherRepository.findById(publisherId);
            if (publisher == null)
                return errorResponse("no such publisherEditing with id: " + publisherId, HttpStatus.UNPROCESSABLE_ENTITY);
            book.setPublisher(publisher);
            book.setEdition(edition);
            bookRepository.update(book);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return responseMessage("book's been successfully updated", HttpStatus.OK);
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    @CrossOrigin
    public ResponseEntity<String> deleting(@PathVariable("id") long id, HttpServletRequest req) {
        Book book = bookRepository.findById(id);
        if (book == null)
            return errorResponse("no such book with id: "+id, HttpStatus.NOT_FOUND);
        bookRepository.remove(book);
        return responseMessage("book's been deleted", HttpStatus.OK);
    }

    private void validateEditingRequestBody(JsonObject json) {
        if (!json.has("publisherId") || !json.has("edition"))
            throw new MalformedRequestException();
    }

    private void validateCreationRequestBody(JsonObject json) {
        if (!json.has("publisherId") || !json.has("workId") || !json.has("edition"))
            throw new MalformedRequestException();
    }

}
