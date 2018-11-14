package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.author.Author;

public class AuthorView {

    private long id;

    private String firstName;

    private String lastName;

    private String url;

    private String middleName;

    public AuthorView(Author author) {
        this.id = author.getId();
        this.firstName = author.getName().getFirstName();
        this.middleName = author.getName().getMiddleName();
        this.lastName = author.getName().getLastName();
        this.url = "http://localhost:8080/lise/adm/authors/"+id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUrl() {
        return url;
    }

    public long getId() {
        return id;
    }
}
