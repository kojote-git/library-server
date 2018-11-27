package com.jkojote.libraryserver.application.mailing;

import javax.mail.Address;
import java.util.List;

public interface MessageData {

    String getSubject();

    Object getContent();

    String getMimeType();

    List<Address> getRecipients();
}
