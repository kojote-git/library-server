package com.jkojote.libraryserver.application.mailing;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesAuthenticator extends Authenticator {

    private PasswordAuthentication auth;


    public PropertiesAuthenticator(String path)
    throws IOException {
        try (InputStream in = new FileInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            String mail = props.getProperty("mail");
            String password = props.getProperty("password");
            if (mail == null)
                throw new IllegalStateException("properties must have 'mail' property");
            if (password == null)
                throw new IllegalStateException("properties must have 'password' property");
            auth = new PasswordAuthentication(mail, password);
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }
}
