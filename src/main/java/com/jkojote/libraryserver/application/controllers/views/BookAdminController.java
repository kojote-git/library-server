package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/adm/books")
public class BookAdminController {

    private DomainRepository<Book> bookRepository;

    private final ModelAndView creation;

    @Autowired
    public BookAdminController(@Qualifier("bookRepository")
                               DomainRepository<Book> bookRepository) {
        this.bookRepository = bookRepository;
        creation = new ModelAndView();
        creation.setViewName("book/create");
        creation.addAllObjects(AdminController.getEntitiesHrefs());
        creation.setStatus(HttpStatus.OK);
    }

    @GetMapping("")
    @AuthorizationRequired
    public ModelAndView books(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("book/books");
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }

    @GetMapping("{id}")
    @AuthorizationRequired
    public ModelAndView bookEditing(HttpServletRequest req, @PathVariable("id") long id) {
        Book book = bookRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView();
        if (book == null) {
            return AdminController.getNotFound();
        }
        modelAndView.addObject("book", book);
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        modelAndView.setViewName("book/book");
        return modelAndView;
    }

    @GetMapping("creation")
    @AuthorizationRequired
    public ModelAndView creation(HttpServletRequest req) {
        return creation;
    }
}
