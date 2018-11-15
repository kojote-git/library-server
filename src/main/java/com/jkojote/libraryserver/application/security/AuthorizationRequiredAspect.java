package com.jkojote.libraryserver.application.security;

import com.jkojote.libraryserver.application.controllers.Util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Aspect
public class AuthorizationRequiredAspect {

    private AuthorizationService authorizationService = AdminAuthorizationService.getService();

    @Pointcut("@annotation(AuthorizationRequired)")
    public void withAuthorizationRequired() { }

    @Pointcut("execution(public org.springframework.http.ResponseEntity<String> *(..))")
    public void anyRestController() { }

    @Pointcut("execution(public org.springframework.web.servlet.ModelAndView *(..))")
    public void anyModelAndViewController() { }

    @Around("anyRestController() && withAuthorizationRequired()")
    public ResponseEntity<String> authorize(ProceedingJoinPoint pjp) {
        HttpServletRequest req = getRequest(pjp);
        if (req == null)
            throw new IllegalStateException("methods annotated with " +
                    "@AuthorizationRequired must provide request parameter");
        try {
            authorize(req);
            return (ResponseEntity<String>) pjp.proceed();
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Around("anyModelAndViewController() && withAuthorizationRequired()")
    public ModelAndView authorizeModelAnView(ProceedingJoinPoint pjp) {
        HttpServletRequest req = getRequest(pjp);
        if (req == null)
            throw new IllegalStateException("methods annotated with " +
                "@AuthorizationRequired must provide request parameter");
        try {
            authorizeViaCookies(req);
            return (ModelAndView)pjp.proceed();
        } catch (AuthorizationException e) {
            ModelAndView unauthorized = new ModelAndView();
            unauthorized.setViewName("redirect:/adm/authorize-first");
            return unauthorized;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private HttpServletRequest getRequest(ProceedingJoinPoint jp) {
        Object[] args = jp.getArgs();
        HttpServletRequest req = null;
        for (Object arg : args) {
            if (arg instanceof ServletRequest) {
                req = (HttpServletRequest) arg;
                break;
            }
        }
        return req;
    }

    private void authorize(HttpServletRequest req) {
        String login = req.getHeader("Login");
        String accessToken = req.getHeader("Access-token");
        if (login == null || accessToken == null)
            throw new AuthorizationException("no credentials present", UNAUTHORIZED);
        if (!authorizationService.authorizeWithToken(login, accessToken))
            throw new AuthorizationException("wrong credentials", UNAUTHORIZED);
    }

    private void authorizeViaCookies(HttpServletRequest req) {
        Optional<Cookie> optionalTokenCookie = Util.extractCookie("accessToken", req);
        Optional<Cookie> optionalLoginCookie = Util.extractCookie("login", req);
        if (!optionalTokenCookie.isPresent() || !optionalLoginCookie.isPresent())
            throw new AuthorizationException("no credentials present", UNAUTHORIZED);
        Cookie login = optionalLoginCookie.get();
        Cookie token = optionalTokenCookie.get();
        if (!authorizationService.authorizeWithToken(login.getValue(), token.getValue())) {
            throw new AuthorizationException("wrong credentials", UNAUTHORIZED);
        }
    }
}
