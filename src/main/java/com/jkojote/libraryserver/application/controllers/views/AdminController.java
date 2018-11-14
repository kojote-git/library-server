package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.author.Author;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/adm")
public class AdminController {

    private DomainRepository<Author> authorRepository;

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

    @GetMapping("authors/{id}")
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
    public ModelAndView authors() {
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
