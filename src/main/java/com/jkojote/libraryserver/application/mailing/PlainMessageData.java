package com.jkojote.libraryserver.application.mailing;

import javax.mail.Address;
import java.util.ArrayList;
import java.util.List;

public class PlainMessageData implements MessageData {

    private String subject;

    private Object content;

    private String encoding;

    private String mimeType;

    private List<Address> recipients;

    private PlainMessageData() { }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public List<Address> getRecipients() {
        return recipients;
    }

    public static final class Builder {

        private String subject;

        private Object content;

        private String mimeType;

        private String encoding;

        private List<Address> recipients;

        private boolean autoClear;

        private Builder(boolean autoClear) {
            this.autoClear = autoClear;
        }

        public static Builder create(boolean autoClear) {
            return new Builder(autoClear);
        }

        public Builder addRecipient(Address address) {
            if (this.recipients == null)
                this.recipients = new ArrayList<>();
            this.recipients.add(address);
            return this;
        }

        public Builder withAllRecipients(List<Address> addresses) {
            if (this.recipients == null)
                this.recipients = new ArrayList<>(addresses);
            else
                this.recipients.addAll(addresses);
            return this;
        }

        public Builder setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder setContent(Object content) {
            this.content = content;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public PlainMessageData build() {
            PlainMessageData res = new PlainMessageData();
            res.recipients = this.recipients;
            res.content = this.content;
            res.mimeType = this.mimeType;
            res.subject = this.subject;
            if (this.encoding == null)
                this.encoding = "utf-8";
            res.encoding = this.encoding;
            if (autoClear)
                clear();
            return res;
        }

        public void clear() {
            this.content = null;
            this.subject = null;
            this.recipients = null;
            this.encoding = null;
        }
    }
}
