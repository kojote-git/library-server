package com.jkojote.libraryserver.application.mailing;

import javax.mail.Address;
import java.util.List;

public interface MessageData {

    String getSubject();

    Object getContent();

    default String getEncoding() {
        return "utf-8";
    }

    String getMimeType();

    List<Address> getRecipients();
}
