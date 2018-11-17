package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Subject;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.OrdinaryText;
import com.jkojote.library.values.Text;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.exceptions.MalformedRequestException;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jkojote.library.domain.model.work.Work.WorkBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.jkojote.libraryserver.application.controllers.utils.Util.errorResponse;
import static com.jkojote.libraryserver.application.controllers.utils.Util.responseMessage;

@RestController
@RequestMapping("/rest/works")
public class WorkController {

    private DomainRepository<Work> workRepository;

    private DomainRepository<Author> authorRepository;

    private HttpHeaders defaultHeaders;

    private JsonParser jsonParser;

    private JsonConverter<Work> workJsonConverter;

    private JsonConverter<Author> authorJsonConverter;

    @Autowired
    public WorkController(@Qualifier("workRepository")
                          DomainRepository<Work> workRepository,
                          @Qualifier("authorRepository")
                          DomainRepository<Author> authorRepository,
                          @Qualifier("workJsonConverter")
                          JsonConverter<Work> workJsonConverter,
                          @Qualifier("authorJsonConverter")
                          JsonConverter<Author> authorJsonConverter) {
        this.authorRepository = authorRepository;
        this.workRepository = workRepository;
        this.jsonParser = new JsonParser();
        this.defaultHeaders = new HttpHeaders();
        this.workJsonConverter = workJsonConverter;
        this.authorJsonConverter = authorJsonConverter;
        defaultHeaders.set("Content-Type", "application/json");
    }

    @GetMapping("/")
    @CrossOrigin
    public ResponseEntity<String> getAll() {
        List<Work> works = workRepository.findAll();
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        obj.add("works", array);
        for (Work w : works) {
            array.add(workJsonConverter.convertToJson(w));
        }
        return new ResponseEntity<>(obj.toString(), HttpStatus.OK);
    }

    @AuthorizationRequired
    @PostMapping("creation")
    public ResponseEntity<String> createWork(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            validateCreationRequestBody(json);
            JsonArray authorsJson = json.getAsJsonArray("authors");
            JsonArray subjectsJson = json.getAsJsonArray("subjects");
            List<Author> authors = new ArrayList<>();
            for (JsonElement authorId : authorsJson) {
                long id = authorId.getAsLong();
                Author a = authorRepository.findById(id);
                if (a == null)
                    return errorResponse("no author with such id exists: " + id, HttpStatus.UNPROCESSABLE_ENTITY);
                authors.add(a);
            }
            long workId = workRepository.nextId();
            String title = json.get("title").getAsString();
            String description = json.get("description").getAsString();
            Work work = WorkBuilder.aWork()
                    .withId(workId)
                    .withTitle(title)
                    .withAuthors(authors)
                    .withDescription(OrdinaryText.of(description))
                    .build();
            for (JsonElement subject : subjectsJson) {
                work.addSubject(Subject.of(subject.getAsString()));
            }
            workRepository.save(work);
            return responseMessage("{\"id\":\""+workId+"\"}", HttpStatus.CREATED);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getWork(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("work with specified id doesn't exist yet", defaultHeaders,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(workJsonConverter.convertToString(work),
                defaultHeaders, HttpStatus.OK);
    }

    @GetMapping("{id}/description")
    @CrossOrigin
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
    @CrossOrigin
    public ResponseEntity<String> getAuthors(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("no such work with id: " + id, defaultHeaders,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Author a : work.getAuthors()) {
            array.add(authorJsonConverter.convertToJson(a));
        }
        json.add("authors", array);
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    @GetMapping("{id}/subjects")
    @CrossOrigin
    public ResponseEntity<String> getSubjects(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("no such work with id: " + id, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Subject s : work.getSubjects()) {
            array.add(s.asString());
        }
        json.add("subjects", array);
        return new ResponseEntity<>(json.toString(), defaultHeaders, HttpStatus.OK);
    }

    @AuthorizationRequired
    @PutMapping("{id}/editing")
    public ResponseEntity<String> editWork(@PathVariable("id") long id, HttpServletRequest req)
    throws IOException {
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
            validateEditingRequestBody(json);
            if (json.has("authors")) {
                JsonArray array = json.get("authors").getAsJsonArray();
                compareAndEditAuthors(work, array);
            }
            if (json.has("subjects")) {
                JsonArray array = json.get("subjects").getAsJsonArray();
                compareAndEditSubjects(work, array);
            }
            String title = json.get("title").getAsString();
            Text t = OrdinaryText.of(json.get("description").getAsString());
            work.setDescription(t);
            work.changeTitle(title);
            workRepository.update(work);
            return responseMessage("work has been updated", HttpStatus.OK);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    public ResponseEntity<String> delete(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null)
            return errorResponse("work with such id"+id+"doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        workRepository.remove(work);
        return responseMessage("work has been successfully deleted", HttpStatus.OK);
    }

    private void validateCreationRequestBody(JsonObject json) {
        if (!json.has("authors") || !json.has("subjects") || !json.has("title") || !json.has("description"))
            throw new MalformedRequestException();
    }

    private void validateEditingRequestBody(JsonObject json) {
        if (!json.has("title") || !json.has("description"))
            throw new MalformedRequestException();
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

    private void compareAndEditSubjects(Work work, JsonArray subjectsJson) {
        for (JsonElement e: subjectsJson) {
            JsonObject o = e.getAsJsonObject();
            Subject s = Subject.of(o.get("subject").getAsString());
            if (o.get("action").getAsString().equals("add")) {
                work.addSubject(s);
            } else {
                work.removeSubject(s);
            }
        }
    }
}
