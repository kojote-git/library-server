package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.Name;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.QueryToJsonRunner;
import com.jkojote.libraryserver.application.controllers.utils.Context;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.application.controllers.utils.Queries;
import com.jkojote.libraryserver.application.exceptions.MalformedRequestException;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
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
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.*;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/rest/authors")
@SuppressWarnings("unchecked")
public class AuthorController {

    private DomainRepository<Author> authorRepository;

    private DomainRepository<Work> workRepository;

    private JsonConverter<Author> authorJsonConverter;

    private JsonConverter<Work> workJsonConverter;

    private EntityUrlParamsFilter<Author> authorFilter;

    private QueryToJsonRunner queryRunner;

    private ITemplateEngine templateEngine;

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
            JsonConverter<Work> workJsonConverter,
            @Qualifier("authorFilter")
            EntityUrlParamsFilter<Author> authorFilter,
            QueryToJsonRunner queryRunner,
            ITemplateEngine templateEngine) {
        this.authorRepository = authorRepository;
        this.workRepository = workRepository;
        this.jsonParser = new JsonParser();
        this.authorJsonConverter = authorJsonConverter;
        this.workJsonConverter = workJsonConverter;
        this.authorFilter = authorFilter;
        this.queryRunner = queryRunner;
        this.templateEngine = templateEngine;
    }

    @AuthorizationRequired
    @PostMapping("creation")
    public ResponseEntity<String> creation(HttpServletRequest req) throws IOException {
        long id = authorRepository.nextId();
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            json.add("id", new JsonPrimitive(id));
            validateCreationRequestBody(json);
            String firstName = json.get("firstName").getAsString();
            String middleName = json.get("middleName").getAsString();
            String lastName = json.get("lastName").getAsString();
            Name name = Name.of(firstName, middleName, lastName);
            Author author = Author.AuthorBuilder.anAuthor()
                    .withId(id)
                    .withName(name)
                    .build();
            authorRepository.save(author);
            return responseEntityJson("{\"id\":"+id+"}", HttpStatus.CREATED);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("")
    @CrossOrigin
    public ResponseEntity<String> getAll(HttpServletRequest req) {
        JsonArray array = new JsonArray();
        JsonObject resp = new JsonObject();
        String url = req.getQueryString();
        List<Author> authors = authorFilter.findAllQueryString(url);
        for (Author a : authors)
            array.add(authorJsonConverter.convertToJson(a));
        resp.add("authors", array);
        return responseEntityJson(resp.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getAuthor(@PathVariable("id") long id) {
        Author author = authorRepository.findById(id);
        if (author == null) {
            return errorResponse("no author with such id: " + id, HttpStatus.NOT_FOUND);
        }
        return responseEntityJson(authorJsonConverter.convertToString(author), HttpStatus.OK);
    }

    @GetMapping("{id}/dstats")
    @CrossOrigin
    public ResponseEntity<String> downloadStatistics(@PathVariable("id") long id) {
        Author a = authorRepository.findById(id);
        if (a == null)
            return errorResponse("no such author with id " + id, HttpStatus.NOT_FOUND);
        JsonObject resp = queryRunner.runQuery(Queries.AUTHOR_STATISTICS, id);
        return responseEntityJson(resp.toString(), HttpStatus.OK);
    }

    @GetMapping("report")
    @CrossOrigin
    public ResponseEntity<String> report() {
        JsonObject resp = queryRunner.runQuery(Queries.AUTHORS_REPORT);
        return responseEntityJson(resp.toString(), HttpStatus.OK);
    }

    @GetMapping("report/html")
    @CrossOrigin
    public ResponseEntity<String> reportHtml() {
        JsonObject resp = queryRunner.runQuery(Queries.AUTHORS_REPORT);
        Spliterator<JsonElement> iterator = resp.getAsJsonArray("rows").spliterator();
        List<List<String>> rows = StreamSupport.stream(iterator, false)
                .map(e -> StreamSupport.stream(e.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsString).collect(toList()))
                .collect(toList());
        IContext ctx = Context.builder()
                .add("rows", rows)
                .build();
        String res = templateEngine.process("author/report", ctx);
        return responseHtml(res, HttpStatus.OK);
    }

    @GetMapping("{id}/works")
    @CrossOrigin
    public ResponseEntity<String> getWorks(@PathVariable("id") long id) {
        Author author = authorRepository.findById(id);
        if (author == null) {
            return errorResponse("no author with such id: " + id, HttpStatus.NOT_FOUND);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Work w : author.getWorks()) {
            array.add(workJsonConverter.convertToJson(w));
        }
        json.add("works", array);
        return responseEntityJson(json.toString(), HttpStatus.OK);
    }

    @AuthorizationRequired
    @PutMapping("{id}/editing")
    public ResponseEntity<String> editAuthor(@PathVariable("id") long id, HttpServletRequest req) {
        Author a = authorRepository.findById(id);
        if (a == null) {
            return errorResponse("no author with such id:" + id, HttpStatus.NOT_FOUND);
        }
        try {
            JsonObject json;
            try (BufferedReader reader = req.getReader()) {
                json = jsonParser.parse(reader).getAsJsonObject();
            }
            if (json.has("works")) {
                compareAndEditWorks(a, json.get("works").getAsJsonArray());
            }
            validateEditingRequestBody(json);
            String firstName = json.get("firstName").getAsString(),
                   middleName = json.get("middleName").getAsString(),
                   lastName  = json.get("lastName").getAsString();
            Name newName = Name.of(firstName, middleName, lastName);
            a.setName(newName);
            authorRepository.update(a);
            return responseMessage("author has been updated", HttpStatus.OK);
        } catch (MalformedRequestException e1) {
            return errorResponse(e1.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateEditingRequestBody(JsonObject json) {
        if (!json.has("firstName") || !json.has("middleName") || !json.has("lastName"))
            throw new MalformedRequestException("request body is malformed");
    }

    private void validateCreationRequestBody(JsonObject jsonObject) {
        validateEditingRequestBody(jsonObject);
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    public ResponseEntity<String> deleteAuthor(@PathVariable("id") long id, HttpServletRequest req) {
        Author a = authorRepository.findById(id);
        if (a == null)
            return errorResponse("no such author with id " + id, HttpStatus.NOT_FOUND);
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
