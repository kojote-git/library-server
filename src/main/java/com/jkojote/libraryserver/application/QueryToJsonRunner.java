package com.jkojote.libraryserver.application;

import com.google.gson.JsonObject;

public interface QueryToJsonRunner {

    JsonObject runQuery(String sql);

    JsonObject runQuery(String sql, Object ... args);
}
