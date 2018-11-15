package com.jkojote.libraryserver.application.exceptions;

public class MalformedRequestException extends RuntimeException {
    public MalformedRequestException() {
        super("request is malformed");
    }

    public MalformedRequestException(String message) {
        super(message);
    }
}
