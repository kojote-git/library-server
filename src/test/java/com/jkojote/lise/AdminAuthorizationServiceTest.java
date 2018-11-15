package com.jkojote.lise;

import com.jkojote.libraryserver.application.security.AdminAuthorizationService;
import com.jkojote.libraryserver.application.security.AuthorizationService;
import org.junit.Test;

import static org.junit.Assert.*;

public class AdminAuthorizationServiceTest {

    private AuthorizationService authorizationService = AdminAuthorizationService.getService();

    @Test
    public void authorizeWithToken() {
        authorizationService.setToken("admin", "abcdefg");
        assertTrue(authorizationService.authorizeWithToken("admin", "abcdefg"));
        assertFalse(authorizationService.authorizeWithToken("admin", null));
        assertFalse(authorizationService.authorizeWithToken("admin", "adawd"));
    }
}
