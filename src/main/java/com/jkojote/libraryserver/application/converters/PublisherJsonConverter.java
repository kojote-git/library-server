package com.jkojote.libraryserver.application.converters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.libraryserver.application.JsonConverter;
import org.springframework.stereotype.Component;

@Component("publisherJsonConverter")
public class PublisherJsonConverter implements JsonConverter<Publisher> {

    @Override
    public Class<Publisher> getFor() {
        return Publisher.class;
    }

    @Override
    public String convertToString(Publisher publisher) {
        return convertToJson(publisher).toString();
    }

    @Override
    public JsonObject convertToJson(Publisher publisher) {
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(publisher.getId()));
        json.add("name", new JsonPrimitive(publisher.getName()));
        return json;
    }
}
