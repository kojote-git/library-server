package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.Util;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.jkojote.libraryserver.application.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/adm")
public class AdminController {

    private DomainRepository<Author> authorRepository;

    private AuthorizationService authorizationService;

    @Autowired
    public AdminController(@Qualifier("authorRepository")
                           DomainRepository<Author> authorRepository,
                           AuthorizationService authorizationService) {
        this.authorRepository = authorRepository;
        this.authorizationService = authorizationService;
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
    public ResponseEntity<String> authorize(HttpServletRequest request) {
        String credentialsHeader = request.getHeader("Credentials");
        String[] credentials = credentialsHeader.split(":");
        if (!authorizationService.authorize(credentials[0], credentials[1]))
            return Util.errorResponse("bad credentials", HttpStatus.UNAUTHORIZED);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "credentials="+credentialsHeader);
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @PostMapping("logout")
    @ResponseBody
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return null;
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
