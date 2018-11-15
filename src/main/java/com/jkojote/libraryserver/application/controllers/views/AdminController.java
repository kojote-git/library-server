package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.Util;
import com.jkojote.libraryserver.application.security.AdminAuthorizationService;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.jkojote.libraryserver.application.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Controller
@RequestMapping("/adm")
public class AdminController {

    private DomainRepository<Author> authorRepository;

    private AuthorizationService authorizationService = AdminAuthorizationService.getService();

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
        resp.addCookie(new Cookie("accessToken", accessToken));
        resp.addCookie(new Cookie("login", login));
        return new ResponseEntity<>("OK", OK);
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

    @GetMapping("authors/{id}")
    @AuthorizationRequired
    public ModelAndView authorEditing(@PathVariable("id") long id, HttpServletRequest req) {
        Author author = authorRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView();
        if (author == null) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("not-found");
            return modelAndView;
        }
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addObject("author", new AuthorView(author));
        modelAndView.setViewName("author/author");
        return modelAndView;
    }

    @GetMapping("authors")
    @AuthorizationRequired
    public ModelAndView authors(HttpServletRequest req) {
        List<Author> authors = authorRepository.findAll();
        List<AuthorView> views = new ArrayList<>();
        ModelAndView modelAndView = new ModelAndView();
        for (Author a : authors) {
            views.add(new AuthorView(a));
        }
        modelAndView.addObject("authors", views);
        modelAndView.setViewName("author/authors");
        return modelAndView;
    }
}
