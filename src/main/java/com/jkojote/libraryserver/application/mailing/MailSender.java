package com.jkojote.libraryserver.application.mailing;

import javax.mail.Authenticator;
import javax.mail.MessagingException;

public interface MailSender {

    void send(MessageData data, Authenticator authenticator) throws MessagingException;

}
