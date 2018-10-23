package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class Util {

    private static final HttpHeaders DEFAULT_HEADERS = new HttpHeaders();

    static {
        DEFAULT_HEADERS.add("Access-Control-Allow-Origin", "*");
        DEFAULT_HEADERS.set("Content-Type", "application/json");
    }

    public static ResponseEntity<String> errorResponse(String message,
                                                       HttpHeaders headers,
                                                       HttpStatus status) {
        JsonObject json = new JsonObject();
        json.add("error", new JsonPrimitive(message));
        return new ResponseEntity<>(message, headers, status);
    }

    public static ResponseEntity<String> responseMessage(String message, HttpHeaders headers, HttpStatus status) {
        JsonObject json = new JsonObject();
        json.add("responseMessage", new JsonPrimitive(message));
        return new ResponseEntity<>(json.toString(), headers, status);
    }

    public static ResponseEntity<String> errorResponse(String message, HttpStatus status) {
        return errorResponse(message, DEFAULT_HEADERS, status);
    }

    public static ResponseEntity<String> responseMessage(String message, HttpStatus status) {
        return responseMessage(message, DEFAULT_HEADERS, status);
    }
}
