package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.JsonConverter;
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
@RequestMapping("/books")
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

    @GetMapping("/")
    @CrossOrigin
    public ResponseEntity<String> getAll() {
        List<Book> books = bookRepository.findAll();
        JsonArray res = new JsonArray();
        for (Book b : books) {
            res.add(bookJsonConverter.convertToJson(b));
        }
        return new ResponseEntity<>(res.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getBook(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null) {
            return errorResponse("no such book with id: " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(bookJsonConverter.convertToString(book), HttpStatus.OK);
    }

    @GetMapping("{id}/instances")
    @CrossOrigin
    public ResponseEntity<String> getBookInstances(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null) {
            return errorResponse("no such book with id: " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<BookInstance> instances = book.getBookInstances();
        JsonObject response = new JsonObject();
        JsonArray array = new JsonArray();
        for (BookInstance bi : instances) {
            array.add(bookInstanceJsonConverter.convertToJson(bi));
        }
        response.add("instances", array);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("creation")
    @CrossOrigin
    public ResponseEntity<String> creation(ServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            long publisherId = json.get("publisherId").getAsLong();
            long workId = json.get("workId").getAsLong();
            int edition = json.get("edition").getAsInt();
            Publisher publisher = publisherRepository.findById(publisherId);
            if (publisher == null)
                return errorResponse("no such publisher with id: "+publisherId, HttpStatus.UNPROCESSABLE_ENTITY);
            Work work = workRepository.findById(workId);
            if (work == null)
                return errorResponse("no such work with id: "+workId, HttpStatus.UNPROCESSABLE_ENTITY);
            long id = bookRepository.nextId();
            Book book = new Book(id, work, publisher, edition, new ArrayList<>());
            bookRepository.save(book);
            return new ResponseEntity<>("{\"id\":"+id+"}", HttpStatus.CREATED);
        }
    }

    @PutMapping("{id}/editing")
    @CrossOrigin
    public ResponseEntity<String> editing(@PathVariable("id") long id, ServletRequest req)
    throws IOException {
        Book book = bookRepository.findById(id);
        if (book == null)
            return errorResponse("no such book with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            int edition = json.get("edition").getAsInt();
            long publisherId = json.get("publisherId").getAsLong();
            Publisher publisher = publisherRepository.findById(publisherId);
            if (publisher == null)
                return errorResponse("no such publisher with id: " + publisherId, HttpStatus.UNPROCESSABLE_ENTITY);
            book.setPublisher(publisher);
            book.setEdition(edition);
            bookRepository.update(book);
        }
        return responseMessage("book's been successfully updated", HttpStatus.OK);
    }

    @DeleteMapping("{id}/deleting")
    @CrossOrigin
    public ResponseEntity<String> deleting(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null)
            return errorResponse("no such book with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        bookRepository.remove(book);
        return responseMessage("book's been deleted", HttpStatus.OK);
    }

}
