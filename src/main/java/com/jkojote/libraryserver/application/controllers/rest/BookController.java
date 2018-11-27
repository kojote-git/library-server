package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.QueryToJsonRunner;
import com.jkojote.libraryserver.application.controllers.utils.Context;
import com.jkojote.libraryserver.application.controllers.utils.Queries;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParser;
import com.jkojote.libraryserver.application.exceptions.MalformedRequestException;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.neovisionaries.i18n.LanguageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.*;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/rest/books")
public class BookController {

    private JsonParser jsonParser;

    private DomainRepository<Book> bookRepository;

    private DomainRepository<Publisher> publisherRepository;

    private DomainRepository<Work> workRepository;

    private JsonConverter<Book> bookJsonConverter;

    private JsonConverter<BookInstance> bookInstanceJsonConverter;

    private QueryToJsonRunner queryRunner;

    private QueryStringParser queryStringParser;

    private ITemplateEngine templateEngine;

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
                          JsonConverter<BookInstance> bookInstanceJsonConverter,
                          QueryToJsonRunner queryRunner,
                          QueryStringParser queryStringParser,
                          ITemplateEngine templateEngine) {
        this.bookRepository = bookRepository;
        this.jsonParser = new JsonParser();
        this.workRepository = workRepository;
        this.publisherRepository = publisherRepository;
        this.bookJsonConverter = bookJsonConverter;
        this.bookInstanceJsonConverter = bookInstanceJsonConverter;
        this.queryRunner = queryRunner;
        this.queryStringParser = queryStringParser;
        this.templateEngine = templateEngine;
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

    @GetMapping("report")
    @CrossOrigin
    public ResponseEntity<String> report(HttpServletRequest req) {
        try {
            JsonObject resp = getReport(req);
            return responseEntityJson(resp.toString(), HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return errorResponse("incorrect date format; date format is 'YYYY-mm-dd", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @CrossOrigin
    @GetMapping(value = "report/html")
    public ResponseEntity<String> reportHtml(HttpServletRequest req) {
        try {
            JsonObject resp = getReport(req);
            Spliterator<JsonElement> iterator = resp.getAsJsonArray("rows").spliterator();
            List<List<String>> rows = StreamSupport.stream(iterator, false)
                    .map(arr -> StreamSupport.stream(arr.getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsString).collect(toList()))
                    .collect(toList());
            IContext ctx = Context.builder()
                    .add("rows", rows)
                    .add("dateBegin", resp.get("dateBegin").getAsString())
                    .add("dateEnd", resp.get("dateEnd").getAsString())
                    .build();
            String res = templateEngine.process("book/report", ctx);
            return responseHtml(res, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return errorResponse("incorrect date format; date format is YYYY-mm-dd", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private JsonObject getReport(HttpServletRequest req) {
        String queryString = req.getQueryString();
        Map<String, String> params = queryStringParser.getParamsFromQueryString(queryString);
        String dateBeginParam = params.getOrDefault("dateBegin", "1000-01-01");
        String dateEndParam = params.getOrDefault("dateEnd", "9999-12-31");
        LocalDate dateBegin = LocalDate.parse(dateBeginParam, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate dateEnd = LocalDate.parse(dateEndParam, DateTimeFormatter.ISO_LOCAL_DATE);
        JsonObject resp = queryRunner.runQuery(Queries.POPULAR_BOOKS, dateBegin, dateEnd);
        resp.add("dateBegin", new JsonPrimitive(dateBeginParam));
        resp.add("dateEnd", new JsonPrimitive(dateEndParam));
        return resp;
    }

    @GetMapping("{id}/dstats")
    @CrossOrigin
    public ResponseEntity<String> downloadStatistics(@PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        if (book == null)
            return errorResponse("no such book with id " + id, HttpStatus.NOT_FOUND);
        JsonObject resp = queryRunner.runQuery(Queries.BOOK_STATISTICS, id);
        return responseEntityJson(resp.toString(), HttpStatus.OK);
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
            String title = json.get("title").getAsString();
            LanguageCode lang = LanguageCode.getByCode(json.get("lang").getAsString());
            if (lang == null)
                return errorResponse("invalid language code", HttpStatus.UNPROCESSABLE_ENTITY);
            Publisher publisher = publisherRepository.findById(publisherId);
            if (publisher == null)
                return errorResponse("no such publisherEditing with id: " + publisherId, HttpStatus.UNPROCESSABLE_ENTITY);
            book.setPublisher(publisher);
            book.setLanguage(lang);
            book.setTitle(title);
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
        if (!json.has("publisherId") || !json.has("edition") || !json.has("lang") || !json.has("title"))
            throw new MalformedRequestException();
    }

    private void validateCreationRequestBody(JsonObject json) {
        if (!json.has("publisherId") || !json.has("workId") || !json.has("edition"))
            throw new MalformedRequestException();
    }

}
