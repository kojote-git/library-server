package com.jkojote.libraryserver.application.security;

import org.mindrot.jbcrypt.BCrypt;

public class AdminAuthorizationService implements AuthorizationService {

    private static final AuthorizationService THIS = new AdminAuthorizationService();

    private final String HASHED_PASSWORD = "$2a$10$ir8J9d58MGxOCNOMl5PBMOPPJMjx22Q2.VMrLkgSB/G88kKRhMRqq";

    private String accessToken;

    private AdminAuthorizationService() {

    }

    public static AuthorizationService getService() {
        return THIS;
    }

    @Override
    public boolean authorize(String user, String password) {
        if (!user.equals("admin"))
            return false;
        return BCrypt.checkpw(password, HASHED_PASSWORD);
    }

    @Override
    public boolean authorizeWithToken(String user, String accessToken) {
        if (!user.equals("admin"))
            return false;
        if (this.accessToken == null)
            return false;
        return this.accessToken.equals(accessToken);
    }

    @Override
    public boolean setToken(String user, String accessToken) {
        if (!user.equals("admin"))
            return false;
        this.accessToken = accessToken;
        return true;
    }

    @Override
    public String removeToken(String user) {
        if (!user.equals("admin"))
            return "";
        String t = this.accessToken;
        this.accessToken = null;
        return t;
    }

    @Override
    public String getToken(String user) {
        if (!user.equals("admin"))
            return null;
        return accessToken;
    }
}
