package com.jkojote.libraryserver.application.controllers.utils;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class QueryStringParserImpl implements QueryStringParser {

    @Override
    public Map<String, String> getParams(String url) {
        if (url == null)
            return Collections.emptyMap();
        int startIndex = url.indexOf("?");
        if (startIndex == -1)
            return Collections.emptyMap();
        String allParams = url.substring(startIndex + 1, url.length());
        return getParamsFromQueryString(allParams);
    }

    @Override
    public Map<String, String> getParamsFromQueryString(String queryString) {
        try {
            if (queryString == null)
                return Collections.emptyMap();
            Map<String, String> pairs = new HashMap<>();
            queryString = URLDecoder.decode(queryString, "UTF-8");
            String[] params = queryString.split("&");
            for (String param : params) {
                int idx = param.indexOf("=");
                if (idx == -1)
                    continue;
                String paramName = param.substring(0, idx);
                String paramValue = param.substring(idx + 1);
                if (!pairs.containsKey(paramName))
                    pairs.put(paramName, paramValue);
            }
            return pairs;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
