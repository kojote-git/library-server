package com.jkojote.libraryserver.application.controllers.rest;

import com.google.gson.*;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.model.work.Subject;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.values.OrdinaryText;
import com.jkojote.library.values.Text;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.application.controllers.utils.EntityUrlParamsFilter;
import com.jkojote.libraryserver.application.exceptions.MalformedRequestException;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.neovisionaries.i18n.LanguageCode;
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
import static com.jkojote.libraryserver.application.controllers.utils.Util.responseEntityJson;
import static com.jkojote.libraryserver.application.controllers.utils.Util.responseMessage;

@RestController
@RequestMapping("/rest/works")
public class WorkController {

    private DomainRepository<Work> workRepository;

    private DomainRepository<Author> authorRepository;

    private JsonParser jsonParser;

    private JsonConverter<Work> workJsonConverter;

    private JsonConverter<Author> authorJsonConverter;

    private EntityUrlParamsFilter<Work> workFilter;

    @Autowired
    public WorkController(@Qualifier("workRepository")
                          DomainRepository<Work> workRepository,
                          @Qualifier("authorRepository")
                          DomainRepository<Author> authorRepository,
                          @Qualifier("workJsonConverter")
                          JsonConverter<Work> workJsonConverter,
                          @Qualifier("authorJsonConverter")
                          JsonConverter<Author> authorJsonConverter,
                          @Qualifier("workFilter")
                          EntityUrlParamsFilter<Work> workFilter) {
        this.authorRepository = authorRepository;
        this.workRepository = workRepository;
        this.jsonParser = new JsonParser();
        this.workJsonConverter = workJsonConverter;
        this.authorJsonConverter = authorJsonConverter;
        this.workFilter = workFilter;
    }

    @GetMapping("")
    @CrossOrigin
    public ResponseEntity<String> getAll(HttpServletRequest req) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        String queryString = req.getQueryString();
        List<Work> works = workFilter.findAllQueryString(queryString);
        obj.add("works", array);
        for (Work w : works) {
            array.add(workJsonConverter.convertToJson(w));
        }
        return responseEntityJson(obj.toString(), HttpStatus.OK);
    }

    /**
     * Reads body of the request and creates new work; returns assigned id.
     * The request body is represented by json. Example:
     * <pre>
     * {
     *   "title":"title",
     *   "description":"description",
     *   "authors":[1,2],
     *   "subjects":["subject1", "subject2"]
     * }
     * </pre>
     * <b>All properties are required<br/>
     *
     * @param req request, needed for authorization and for reading json
     * @return response that contains id of newly created work
     * @throws IOException
     */
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
                    return errorResponse("no author with such id exists: " + id, HttpStatus.NOT_FOUND);
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
            return responseEntityJson("{\"id\":\""+workId+"\"}", HttpStatus.CREATED);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getWork(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("work with specified id doesn't exist yet", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(workJsonConverter.convertToString(work), HttpStatus.OK);
    }

    @GetMapping("{id}/description")
    @CrossOrigin
    public ResponseEntity<String> getDescription(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("work with specified id doesn't exist yet", HttpStatus.NOT_FOUND);
        }
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(id));
        json.add("description", new JsonPrimitive(work.getDescription().toString()));
        return responseEntityJson(json.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}/authors")
    @CrossOrigin
    public ResponseEntity<String> getAuthors(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("no such work with id: " + id, HttpStatus.NOT_FOUND);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Author a : work.getAuthors()) {
            array.add(authorJsonConverter.convertToJson(a));
        }
        json.add("authors", array);
        return responseEntityJson(json.toString(), HttpStatus.OK);
    }

    @GetMapping("{id}/subjects")
    @CrossOrigin
    public ResponseEntity<String> getSubjects(@PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null) {
            return errorResponse("no such work with id: " + id, HttpStatus.NOT_FOUND);
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        for (Subject s : work.getSubjects()) {
            array.add(s.asString());
        }
        json.add("subjects", array);
        return responseEntityJson(json.toString(), HttpStatus.OK);
    }

    @PutMapping("{id}/editing")
    @AuthorizationRequired
    public ResponseEntity<String> editWork(@PathVariable("id") long id, HttpServletRequest req)
            throws IOException {
        try {
            Work work = workRepository.findById(id);
            if (work == null) {
                return errorResponse("no such work with id: " + id, HttpStatus.NOT_FOUND);
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
            String lang = json.get("lang").getAsString();
            LanguageCode code = LanguageCode.getByCode(lang);
            if (code == null)
                return errorResponse("invalid language code", HttpStatus.UNPROCESSABLE_ENTITY);
            work.setDescription(t);
            work.changeTitle(title);
            work.setLanguage(LanguageCode.getByCode(lang));
            workRepository.update(work);
            return responseMessage("work has been updated", HttpStatus.OK);
        } catch (MalformedRequestException e) {
            return errorResponse(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @AuthorizationRequired
    @DeleteMapping("{id}/deleting")
    public ResponseEntity<String> delete(HttpServletRequest req, @PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        if (work == null)
            return errorResponse("work with such id"+id+"doesn't exists", HttpStatus.NOT_FOUND);
        workRepository.remove(work);
        return responseMessage("work has been successfully deleted", HttpStatus.OK);
    }

    private void validateCreationRequestBody(JsonObject json) {
        if (!json.has("authors") || !json.has("subjects") || !json.has("title") || !json.has("description") ||
                !json.has("lang"))
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
