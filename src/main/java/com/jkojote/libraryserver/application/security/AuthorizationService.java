package com.jkojote.libraryserver.application.security;

public interface AuthorizationService {

    boolean authorize(String user, String password);
}
