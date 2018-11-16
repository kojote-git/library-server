package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.Util;
import com.jkojote.libraryserver.application.security.AdminAuthorizationService;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.jkojote.libraryserver.application.security.AuthorizationService;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Controller
@RequestMapping("/adm")
public class AdminController {

    private DomainRepository<Author> authorRepository;

    private AuthorizationService authorizationService = AdminAuthorizationService.getService();

    private static final Map<String, String> ENTITIES_HREF;

    private static final Map<String, String> ENTITIES_HREF_VIEW;

    static {
        ENTITIES_HREF = new HashMap<>();
        ENTITIES_HREF.put("authorsHref", WebConfig.URL + "adm/authors");
        ENTITIES_HREF.put("booksHref", WebConfig.URL + "adm/books");
        ENTITIES_HREF.put("bookInstancesHref", WebConfig.URL + "adm/bookInstances");
        ENTITIES_HREF.put("publishersHref", WebConfig.URL + "adm/publishers");
        ENTITIES_HREF.put("worksHref", WebConfig.URL + "adm/works");
        ENTITIES_HREF_VIEW = Collections.unmodifiableMap(ENTITIES_HREF);
    }

    public static Map<String, String> getEntitiesHrefs() {
        return ENTITIES_HREF_VIEW;
    }

    @Autowired
    public AdminController(@Qualifier("authorRepository")
                           DomainRepository<Author> authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public ModelAndView adminPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin-page");
        return modelAndView;
    }

    @GetMapping("authorize-first")
    public String authorizeFirst() { return "authorize-first"; }

    @PostMapping("authorization")
    @ResponseBody
    public ResponseEntity<String> authorize(HttpServletRequest req, HttpServletResponse resp) {
        String login = req.getHeader("Login");
        String password = req.getHeader("Password");
        if (login == null || password == null)
            return Util.errorResponse("no credentials present", UNAUTHORIZED);
        if (!authorizationService.authorize(login, password))
            return Util.errorResponse("wrong credentials", UNAUTHORIZED);
        String accessToken = Util.randomAlphaNumeric();
        authorizationService.setToken(login, accessToken);
        resp.addHeader("Access-token", accessToken);
        resp.addCookie(new Cookie("accessToken", accessToken));
        resp.addCookie(new Cookie("login", login));
        return new ResponseEntity<>("authorized", OK);
    }

    @PostMapping("logout")
    @ResponseBody
    public ResponseEntity<String> logout(HttpServletRequest req, HttpServletResponse resp) {
        Optional<Cookie> optionalCookie = Util.extractCookie("accessToken", req);
        if (!optionalCookie.isPresent())
            return new ResponseEntity<>("no credentials present", BAD_REQUEST);
        Cookie cookie = optionalCookie.get();
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        return new ResponseEntity<>("ok", OK);
    }

    @GetMapping("admin-page")
    @AuthorizationRequired
    public ModelAndView adminPage(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin-page");
        modelAndView.addAllObjects(getEntitiesHrefs());
        return modelAndView;
    }

}
