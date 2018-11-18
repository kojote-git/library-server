package com.jkojote.libraryserver.application.controllers.views;

import com.jkojote.library.domain.model.publisher.Publisher;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/adm/publishers")
public class PublisherAdminController {

    private DomainRepository<Publisher> publisherRepository;

    public PublisherAdminController(@Qualifier("publisherRepository")
                                    DomainRepository<Publisher> publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @GetMapping("")
    @AuthorizationRequired
    public ModelAndView publishers(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.setViewName("publisher/publishers");
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }

    @GetMapping("{id}")
    @AuthorizationRequired
    public ModelAndView publisherEditing(HttpServletRequest req, @PathVariable("id") long id) {
        ModelAndView modelAndView = new ModelAndView();
        Publisher publisher = publisherRepository.findById(id);
        if (publisher == null) {
            return AdminController.getNotFound();
        }
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.setViewName("publisher/publisher");
        modelAndView.addObject("publisher", publisher);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }

    @GetMapping("creation")
    @AuthorizationRequired
    public ModelAndView creation(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("publisher/create");
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }
}
