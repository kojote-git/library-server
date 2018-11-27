package com.jkojote.libraryserver.application.recomendations;

import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.reader.Reader;

import java.util.List;

public interface RecomendationsGenerator {

    default List<Book> getFor(Reader reader) {
        return random();
    }

    List<Book> random();
}
