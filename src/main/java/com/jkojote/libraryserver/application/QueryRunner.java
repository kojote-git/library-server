package com.jkojote.libraryserver.application;

import com.google.gson.JsonObject;

public interface QueryRunner {

    JsonObject runQuery(String sql);
}
