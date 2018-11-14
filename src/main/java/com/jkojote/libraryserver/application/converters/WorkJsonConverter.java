package com.jkojote.libraryserver.application.converters;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jkojote.library.domain.model.work.Work;
import com.jkojote.libraryserver.application.JsonConverter;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.stereotype.Component;

@Component("workJsonConverter")
public class WorkJsonConverter implements JsonConverter<Work> {

    @Override
    public Class<Work> getFor() {
        return Work.class;
    }

    @Override
    public String convertToString(Work work) {
        return convertToJson(work).toString();
    }

    @Override
    public JsonObject convertToJson(Work work) {
        JsonObject res = new JsonObject();
        JsonObject links = new JsonObject();
        links.add("adm", new JsonPrimitive(WebConfig.URL + "adm/works/" + work.getId()));
        links.add("rest", new JsonPrimitive(WebConfig.URL + "rest/works" + work.getId()));
        res.add("id", new JsonPrimitive(work.getId()));
        res.add("title", new JsonPrimitive(work.getTitle()));
        res.add("_links", links);
        return res;
    }
}
