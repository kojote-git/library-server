package com.jkojote.libraryserver.application.controllers.utils.infrastructure.filters;

import com.jkojote.libraryserver.application.controllers.utils.infrastructure.MultiReadHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class CachingRequestBodyFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        MultiReadHttpServletRequest newReq = new MultiReadHttpServletRequest(req);
        chain.doFilter(newReq, response);
    }
}
