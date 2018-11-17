package com.jkojote.libraryserver.application.controllers.utils;

import java.util.Map;

public interface RequestUrlParametersParser {

    Map<String, String> getParams(String url);

    Map<String, String> getParamsFromQueryString(String queryString);

}
