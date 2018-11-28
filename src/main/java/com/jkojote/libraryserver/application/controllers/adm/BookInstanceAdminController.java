package com.jkojote.libraryserver.application.controllers.adm;

import com.jkojote.library.domain.model.book.instance.BookInstance;
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
@RequestMapping("/adm/instances")
public class BookInstanceAdminController {

    private DomainRepository<BookInstance> bookInstanceRepository;

    private final ModelAndView creation;

    @Autowired
    public BookInstanceAdminController(@Qualifier("bookInstanceRepository")
                                       DomainRepository<BookInstance> bookInstanceRepository) {
        this.bookInstanceRepository = bookInstanceRepository;
        this.creation = new ModelAndView();
        creation.addAllObjects(AdminController.getEntitiesHrefs());
        creation.setStatus(HttpStatus.OK);
        creation.setViewName("booInstance/create");
    }

    @GetMapping("{id}")
    @AuthorizationRequired
    public ModelAndView bookInstanceEditing(HttpServletRequest req, @PathVariable("id") long id) {
        BookInstance bookInstance = bookInstanceRepository.findById(id);
        if (bookInstance == null)
            return AdminController.getNotFound();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addObject("bookInstance", bookInstance);
        modelAndView.setViewName("bookInstance/bookInstance");
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }
}
