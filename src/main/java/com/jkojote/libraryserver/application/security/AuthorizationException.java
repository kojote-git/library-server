package com.jkojote.libraryserver.application.security;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends RuntimeException {

    private HttpStatus status;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
