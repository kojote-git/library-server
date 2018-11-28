package com.jkojote.libraryserver.application.controllers.adm;

import com.jkojote.library.domain.model.work.Work;
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
@RequestMapping("/adm/works")
public class WorkAdminController {

    private DomainRepository<Work> workRepository;

    @Autowired
    public WorkAdminController(@Qualifier("workRepository")
                               DomainRepository<Work> workRepository) {
        this.workRepository = workRepository;
    }

    @GetMapping("{id}")
    @AuthorizationRequired
    public ModelAndView workEditing(HttpServletRequest req, @PathVariable("id") long id) {
        Work work = workRepository.findById(id);
        ModelAndView modelAndView = new ModelAndView();
        if (work == null) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("not-found");
            return modelAndView;
        }
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addObject("work", work);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        modelAndView.setViewName("work/work");
        return modelAndView;
    }

    @GetMapping("")
    @AuthorizationRequired
    public ModelAndView works(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        modelAndView.setViewName("work/works");
        return modelAndView;
    }

    @GetMapping("creation")
    @AuthorizationRequired
    public ModelAndView creation(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("work/create");
        modelAndView.addAllObjects(AdminController.getEntitiesHrefs());
        return modelAndView;
    }
}
