package com.jkojote.libraryserver.application.controllers;

import com.google.gson.*;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.OrdinaryText;
import com.jkojote.library.values.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

import static com.jkojote.libraryserver.application.controllers.Util.errorResponse;
import static com.jkojote.libraryserver.application.controllers.Util.responseMessage;

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
                return errorResponse("author doesn't exist", defaultHeaders, HttpStatus.UNPROCESSABLE_ENTITY);
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
            return errorResponse("work with specified id doesn't exist yet", defaultHeaders,
                    HttpStatus.UNPROCESSABLE_ENTITY);
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
            return errorResponse("work with specified id doesn't exist yet", defaultHeaders,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(id));
        json.add("description", new JsonPrimitive(work.getDescription().toString()));
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    @GetMapping("{id}/authors")
    public ResponseEntity<String> getAuthors(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("no such work with id: " + id, defaultHeaders,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Author a : work.getAuthors()) {
            JsonObject t = new JsonObject();
            t.add("id", new JsonPrimitive(a.getId()));
            t.add("firstName", new JsonPrimitive(a.getName().getFirstName()));
            t.add("middleName", new JsonPrimitive(a.getName().getMiddleName()));
            t.add("lastName", new JsonPrimitive(a.getName().getLastName()));
            array.add(t);
        }
        json.add("authors", array);
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    @PutMapping("{id}/editing")
    public ResponseEntity<String> editWork(@PathVariable("id") long id, ServletRequest req) {
        try {
            Work work = workRepository.findById(id);
            if (work == null) {
                return errorResponse("no such work with id: " + id, defaultHeaders,
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
            JsonObject json;
            try (BufferedReader reader = req.getReader()) {
                json = jsonParser.parse(reader).getAsJsonObject();
            }
            if (json.has("authors")) {
                JsonArray array = json.get("authors").getAsJsonArray();
                compareAndEditAuthors(work, array);
            }
            String title = json.get("title").getAsString();
            Text t = OrdinaryText.of(json.get("description").getAsString());
            work.setDescription(t);
            work.changeTitle(title);
            workRepository.update(work);
            return responseMessage("work has been updated", HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /*
     * Removes or adds author to work. The decision to remove or add is based on
     * information that authorJson contains.
     * The format of json array is next:
     *   [
     *     {"action":"remove", "id":"1"},
     *     {"action":"add", "id":"2"}
     *     and so on...
     *   ]
     *
     */
    private void compareAndEditAuthors(Work work, JsonArray authorsJson) {
        for (JsonElement e : authorsJson) {
            JsonObject o = e.getAsJsonObject();
            if (o.get("action").getAsString().equals("add")) {
                long id = o.get("id").getAsLong();
                Author a = authorRepository.findById(id);
                work.addAuthor(a);
            } else {
                long id = o.get("id").getAsLong();
                Author a = authorRepository.findById(id);
                work.removeAuthor(a);
            }
        }
    }
}
