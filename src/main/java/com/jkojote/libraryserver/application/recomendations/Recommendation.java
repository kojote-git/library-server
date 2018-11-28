package com.jkojote.libraryserver.application.recomendations;


import com.jkojote.library.domain.model.book.Book;

public class Recommendation {

    private Book book;

    private String reference;


    /**
     * Builder is used to construct instances
     */
    private Recommendation() { }

    public Book getBook() {
        return book;
    }

    public String getReference() {
        return reference;
    }

    public static final class RecommendationBuilder {

        private Book book;

        private String reference;

        private boolean autoClear;

        private RecommendationBuilder(boolean autoClear) {
            this.autoClear = autoClear;
        }

        public static RecommendationBuilder create(boolean autoClear) {
            return new RecommendationBuilder(autoClear);
        }

        public RecommendationBuilder withBook(Book book) {
            this.book = book;
            return this;
        }

        public RecommendationBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Recommendation build() {
            Recommendation recommendation = new Recommendation();
            recommendation.book = this.book;
            recommendation.reference = this.reference;
            if (autoClear) clear();
            return recommendation;
        }

        public void clear() {
            this.book = null;
            this.reference = null;
        }
    }
}
