package com.jkojote.libraryserver.application.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final String HASHED_PASSWORD = "$2a$10$ir8J9d58MGxOCNOMl5PBMOPPJMjx22Q2.VMrLkgSB/G88kKRhMRqq";

    @Override
    public boolean authorize(String user, String password) {
        if (!user.equals("admin"))
            return false;
        return BCrypt.checkpw(password, HASHED_PASSWORD);

    }
}
