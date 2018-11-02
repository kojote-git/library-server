package com.jkojote.libraryserver.application;

import com.google.gson.JsonObject;

public interface JsonConverter<T> {

    Class<T> getFor();

    String convertToString(T object);

    JsonObject convertToJson(T object);

}
