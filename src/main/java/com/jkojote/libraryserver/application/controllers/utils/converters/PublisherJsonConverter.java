package com.jkojote.libraryserver.application.controllers.utils.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.config.WebConfig;
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
        JsonObject links = new JsonObject();
        links.add("adm", new JsonPrimitive(WebConfig.URL + "adm/publishers/" + publisher.getId()));
        links.add("rest", new JsonPrimitive(WebConfig.URL + "rest/publishers/" + publisher.getId()));
        json.add("id", new JsonPrimitive(publisher.getId()));
        json.add("name", new JsonPrimitive(publisher.getName()));
        json.add("_links", links);
        return json;
    }
}
