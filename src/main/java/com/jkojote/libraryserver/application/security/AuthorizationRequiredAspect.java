package com.jkojote.libraryserver.application.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Aspect
public class AuthorizationRequiredAspect {

    private AuthorizationService authorizationService = new AuthorizationServiceImpl();

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
            ModelAndView forbidden = new ModelAndView();
            forbidden.setViewName("redirect:/adm/authorize-first");
            return forbidden;
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

    private void authorize(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null)
            throw new AuthorizationException("Forbidden: no credentials are present", UNAUTHORIZED);
        int splitIdx = authorization.indexOf(':');
        String name = authorization.substring(0, splitIdx);
        String password = authorization.substring(splitIdx + 1);
        if (!authorizationService.authorize(name, password))
            throw new AuthorizationException("Forbidden: wrong credentials", UNAUTHORIZED);
    }

    private void authorizeViaCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
            throw new AuthorizationException("no credentials present");
        Cookie credentialsCookie = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("credentials")) {
                credentialsCookie = c;
                break;
            }
        }
        if (credentialsCookie == null)
            throw new AuthorizationException("no credentials present");
        String[] credentials = credentialsCookie.getValue().split(":");
        if (!authorizationService.authorize(credentials[0], credentials[1]))
            throw new AuthorizationException("bad credentials", UNAUTHORIZED);
    }
}
