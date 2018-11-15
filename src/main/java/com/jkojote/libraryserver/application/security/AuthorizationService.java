package com.jkojote.libraryserver.application.security;

public interface AuthorizationService {

    boolean authorize(String user, String password);

    boolean authorizeWithToken(String user, String accessToken);

    boolean setToken(String user, String accessToken);

    String removeToken(String user);

    String getToken(String user);
}
