package com.jkojote.libraryserver.application.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.libraryserver.application.JsonConverter;
import org.springframework.stereotype.Component;


@Component("bookJsonConverter")
public class BookJsonConverter implements JsonConverter<Book> {
    @Override
    public Class<Book> getFor() {
        return Book.class;
    }

    @Override
    public String convertToString(Book book) {
        return convertToJson(book).toString();
    }

    @Override
    public JsonObject convertToJson(Book book) {
        JsonObject res = new JsonObject();
        res.add("id", new JsonPrimitive(book.getId()));
        res.add("title", new JsonPrimitive(book.getBasedOn().getTitle()));
        res.add("edition", new JsonPrimitive(book.getEdition()));
        res.add("workId", new JsonPrimitive(book.getBasedOn().getId()));
        res.add("publisherId", new JsonPrimitive(book.getPublisher().getId()));
        return res;
    }
}
