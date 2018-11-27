package com.jkojote.lise;

import com.jkojote.libraryserver.application.mailing.MailSender;
import com.jkojote.libraryserver.application.mailing.MessageData;
import com.jkojote.libraryserver.application.mailing.PlainMessageData;
import com.jkojote.libraryserver.application.mailing.PropertiesAuthenticator;
import com.jkojote.libraryserver.config.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MvcConfig.class)
public class MailSenderTest {

    @Autowired
    private MailSender sender;

    private PlainMessageData.Builder message;

    private Authenticator authenticator;

    public MailSenderTest() throws IOException {
        this.message = PlainMessageData.Builder.create(true);
        this.authenticator = new PropertiesAuthenticator("/home/isaac/Desktop/mailing.properties");
    }

    @Test
    public void send() throws MessagingException {
        MessageData data = message
                .addRecipient(new InternetAddress("a1mostbeastz@gmail.com"))
                .setContent("Do not respond on this message")
                .setMimeType("text/plain")
                .setSubject("Mailing test")
                .build();
        sender.send(data, authenticator);
    }
}
