package com.jkojote.libraryserver.application.controllers.utils;

import com.jkojote.libraryserver.application.Utils;

import javax.rmi.CORBA.Util;
import java.io.File;

public final class Queries {

    public static final String POPULAR_BOOKS;

    public static final String BOOK_STATISTICS;

    public static final String AUTHOR_STATISTICS;

    public static final String AUTHORS_REPORT;

    public static final String RATED_SUBJECTS;

    public static final String SUBJECTS_STATISTICS;

    static {
        try {
            ClassLoader loader = Queries.class.getClassLoader();
            File booksReport = new File(loader.getResource("reports/popular_books.sql").getFile());
            File bookStatistics = new File(loader.getResource("statistics/book_statistics.sql").getFile());
            File authorsReport = new File(loader.getResource("reports/popular_authors.sql").getFile());
            File authorsStatistics = new File(loader.getResource("statistics/author_statistics.sql").getFile());
            File ratedSubjects = new File(loader.getResource("rated_subjects.sql").getFile());
            File subjectStatistics = new File(loader.getResource("statistics/subject_statistics.sql").getFile());
            POPULAR_BOOKS = Utils.readFile(booksReport, true);
            BOOK_STATISTICS = Utils.readFile(bookStatistics, true);
            AUTHOR_STATISTICS = Utils.readFile(authorsStatistics, true);
            AUTHORS_REPORT = Utils.readFile(authorsReport, true);
            RATED_SUBJECTS = Utils.readFile(ratedSubjects, true);
            SUBJECTS_STATISTICS = Utils.readFile(subjectStatistics, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
