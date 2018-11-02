package com.jkojote.libraryserver.application.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.values.Name;
import com.jkojote.libraryserver.application.JsonConverter;
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
        Name name = author.getName();
        res.add("id", new JsonPrimitive(author.getId()));
        res.add("firstName", new JsonPrimitive(name.getFirstName()));
        res.add("middleName", new JsonPrimitive(name.getMiddleName()));
        res.add("lastName", new JsonPrimitive(name.getLastName()));
        return res;
    }
}
