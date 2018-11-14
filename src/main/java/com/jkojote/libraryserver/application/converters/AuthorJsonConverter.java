package com.jkojote.libraryserver.application.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.values.Name;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.stereotype.Component;

@Component("authorJsonConverter")
public class AuthorJsonConverter implements JsonConverter<Author> {
    @Override
    public Class<Author> getFor() {
        return Author.class;
    }

    @Override
    public String convertToString(Author author) {
        return convertToJson(author).toString();
    }

    @Override
    public JsonObject convertToJson(Author author) {
        JsonObject res = new JsonObject();
        JsonObject links = new JsonObject();
        Name name = author.getName();
        links.add("adm", new JsonPrimitive(WebConfig.URL + "adm/authors/" + author.getId()));
        links.add("rest", new JsonPrimitive(WebConfig.URL + "rest/authors/" + author.getId()));
        res.add("id", new JsonPrimitive(author.getId()));
        res.add("firstName", new JsonPrimitive(name.getFirstName()));
        res.add("middleName", new JsonPrimitive(name.getMiddleName()));
        res.add("lastName", new JsonPrimitive(name.getLastName()));
        res.add("_links", links);
        return res;
    }
}
