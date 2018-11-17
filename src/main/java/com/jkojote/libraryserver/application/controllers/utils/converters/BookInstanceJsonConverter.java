package com.jkojote.libraryserver.application.controllers.utils.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.stereotype.Component;

@Component("biJsonConverter")
public class BookInstanceJsonConverter implements JsonConverter<BookInstance> {
    @Override
    public Class<BookInstance> getFor() {
        return BookInstance.class;
    }

    @Override
    public String convertToString(BookInstance bookInstance) {
        return convertToJson(bookInstance).toString();
    }

    @Override
    public JsonObject convertToJson(BookInstance bookInstance) {
        JsonObject res = new JsonObject();
        JsonObject links = new JsonObject();
        links.add("adm", new JsonPrimitive(WebConfig.URL + "adm/instances/" + bookInstance.getId()));
        links.add("rest", new JsonPrimitive(WebConfig.URL + "rest/instances/" + bookInstance.getId()));
        res.add("id", new JsonPrimitive(bookInstance.getId()));
        res.add("format", new JsonPrimitive(bookInstance.getFormat().asString()));
        res.add("isbn13", new JsonPrimitive(bookInstance.getIsbn13().asString()));
        res.add("bookId", new JsonPrimitive(bookInstance.getBook().getId()));
        res.add("_links", links);
        return res;
    }
}
