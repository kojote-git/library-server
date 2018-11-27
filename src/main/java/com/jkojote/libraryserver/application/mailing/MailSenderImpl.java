package com.jkojote.libraryserver.application.mailing;

import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MailSenderImpl implements MailSender {

    private final Properties props;

    public MailSenderImpl(boolean enableDebug) {
        this.props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        if (enableDebug)
            props.put("mail.debug", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
    }

    @Override
    public void send(MessageData data, Authenticator authenticator)
    throws MessagingException {
        Session session = Session.getDefaultInstance(props, authenticator);
        MimeMessage message = new MimeMessage(session);
        message.setSentDate(new Date());
        addRecipients(message, data.getRecipients());
        message.setContent(data.getContent(), data.getMimeType());
        message.setSubject(data.getSubject());
        Transport.send(message);
    }

    private void addRecipients(Message message, List<Address> addresses)
    throws MessagingException {
        for (Address address : addresses) {
            message.addRecipient(Message.RecipientType.TO, address);
        }
    }
}
