package com.jkojote.libraryserver.application.recomendations;

import com.jkojote.library.domain.model.reader.Reader;

import java.util.List;

public interface RecommendationsGenerator {

    default List<Recommendation> getFor(Reader reader) {
        return random();
    }

    default List<Recommendation> getFor(Reader reader, int size) {
        return random(size);
    }

    default List<Recommendation> random(int size) {
        List<Recommendation> random = random();
        if (random.size() == 0)
            return random;
        if (random.size() < size)
            return random.subList(0, random.size());
        return random.subList(0, size);
    }

    List<Recommendation> random();
}
