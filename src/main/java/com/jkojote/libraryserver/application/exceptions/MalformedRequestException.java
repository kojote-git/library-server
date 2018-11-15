package com.jkojote.libraryserver.application.exceptions;

public class MalformedRequestException extends RuntimeException {
    public MalformedRequestException() {
    }

    public MalformedRequestException(String message) {
        super(message);
    }
}
