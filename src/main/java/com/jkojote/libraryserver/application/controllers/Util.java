package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public final class Util {

    private static final HttpHeaders DEFAULT_HEADERS = new HttpHeaders();

    private static final String ALPHA_NUMERIC = "abcdefghijklmnopqrstyvwxyz0123456789";

    private static final Random random = new Random(9999999);

    static {
        DEFAULT_HEADERS.set("Content-Type", "application/json");
    }

    public static ResponseEntity<String> errorResponse(String message,
                                                       HttpHeaders headers,
                                                       HttpStatus status) {
        JsonObject json = new JsonObject();
        json.add("error", new JsonPrimitive(message));
        return new ResponseEntity<>(json.toString(), headers, status);
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

    public static Optional<String> readCookie(String key, HttpServletRequest request) {
        if (request.getCookies() == null)
            return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(key))
                .map(Cookie::getValue)
                .findAny();
    }

    public static Optional<Cookie> extractCookie(String key, HttpServletRequest request) {
        if (request.getCookies() == null)
            return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(key))
                .findAny();
    }

    public static String randomAlphaNumeric() {
        return randomAlphaNumeric(32);
    }

    public static String randomAlphaNumeric(int length) {
        StringBuilder builder = new StringBuilder();
        int alen = ALPHA_NUMERIC.length();
        for (int i = 0; i < length; i++) {
            int k = (Math.abs(random.nextInt()) % alen);
            builder.append(ALPHA_NUMERIC.charAt(k));
        }
        return builder.toString();
    }
}
