package com.jkojote.lise;

import com.jkojote.libraryserver.application.controllers.utils.QueryStringParser;
import com.jkojote.libraryserver.application.controllers.utils.QueryStringParserImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestUrlParametersParserTest {

    private QueryStringParser parser = new QueryStringParserImpl();

    @Test
    public void parse() {
        String url = "http://example.com/site";
        assertTrue(parser.getParams(url).isEmpty());
        url = "http://exmaple.com/site?param1=param1&param2=param2&param3=";
        assertEquals(3, parser.getParams(url).size());
        url = "http://example.com/site?param1=param1";
        assertEquals(1, parser.getParams(url).size());
        url = "http://example.com/site?param1&";
        assertTrue(parser.getParams(url).isEmpty());
    }
}
