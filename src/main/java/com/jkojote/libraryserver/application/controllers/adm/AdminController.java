package com.jkojote.libraryserver.application.controllers.adm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jkojote.libraryserver.application.QueryToJsonRunner;
import com.jkojote.libraryserver.application.controllers.utils.ControllerUtils;
import com.jkojote.libraryserver.application.security.AdminAuthorizationService;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.jkojote.libraryserver.application.security.AuthorizationService;
import com.jkojote.libraryserver.config.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.errorResponse;
import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.responseEntityJson;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Controller
@RequestMapping("/adm")
public class AdminController {

    private AuthorizationService authorizationService = AdminAuthorizationService.getService();

    private static final Map<String, String> ENTITIES_HREF;

    private static final Map<String, String> ENTITIES_HREF_VIEW;

    private static final ModelAndView NOT_FOUND;

    static {
        ENTITIES_HREF = new HashMap<>();
        ENTITIES_HREF.put("authorsHref", WebConfig.URL + "adm/authors");
        ENTITIES_HREF.put("booksHref", WebConfig.URL + "adm/books");
        ENTITIES_HREF.put("publishersHref", WebConfig.URL + "adm/publishers");
        ENTITIES_HREF.put("worksHref", WebConfig.URL + "adm/works");
        ENTITIES_HREF.put("queryHref", WebConfig.URL + "adm/query");
        ENTITIES_HREF.put("createAuthor", WebConfig.URL + "adm/authors/creation");
        ENTITIES_HREF.put("createWork", WebConfig.URL + "adm/works/creation");
        ENTITIES_HREF.put("createBook", WebConfig.URL + "adm/books/creation");
        ENTITIES_HREF.put("createPublisher", WebConfig.URL + "adm/publishers/creation");
        ENTITIES_HREF.put("authorsReport", WebConfig.URL + "rest/authors/report/html");
        ENTITIES_HREF.put("booksReport", WebConfig.URL + "rest/books/report/html");
        ENTITIES_HREF_VIEW = Collections.unmodifiableMap(ENTITIES_HREF);
        NOT_FOUND = new ModelAndView();
        NOT_FOUND.setStatus(HttpStatus.NOT_FOUND);
        NOT_FOUND.setViewName("not-found");
        NOT_FOUND.addAllObjects(getEntitiesHrefs());
    }

    public static Map<String, String> getEntitiesHrefs() {
        return ENTITIES_HREF_VIEW;
    }

    private JsonParser parser;

    private QueryToJsonRunner queryRunner;

    @Autowired
    public AdminController(@Qualifier("queryRunner")
                                   QueryToJsonRunner queryRunner) {
        this.parser = new JsonParser();
        this.queryRunner = queryRunner;
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
            return errorResponse("no credentials present", UNAUTHORIZED);
        if (!authorizationService.authorize(login, password))
            return errorResponse("wrong credentials", UNAUTHORIZED);
        String accessToken = ControllerUtils.randomAlphaNumeric();
        authorizationService.setToken(login, accessToken);
        resp.addHeader("Access-token", accessToken);
        resp.addCookie(new Cookie("accessToken", accessToken));
        resp.addCookie(new Cookie("login", login));
        return new ResponseEntity<>("authorized", OK);
    }

    @PostMapping("logout")
    @ResponseBody
    public ResponseEntity<String> logout(HttpServletRequest req, HttpServletResponse resp) {
        Optional<Cookie> optionalToken = ControllerUtils.extractCookie("accessToken", req);
        Optional<Cookie> optionalLogin = ControllerUtils.extractCookie("login", req);
        if (!optionalToken.isPresent())
            return new ResponseEntity<>("no credentials present", BAD_REQUEST);
        if (optionalLogin.isPresent()) {
            Cookie login = optionalLogin.get();
            login.setMaxAge(0);
            resp.addCookie(login);
        }
        Cookie cookie = optionalToken.get();
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

    @GetMapping("query")
    @AuthorizationRequired
    public ModelAndView queryPage(HttpServletRequest req) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("query-page");
        modelAndView.addAllObjects(getEntitiesHrefs());
        return modelAndView;
    }

    @PostMapping("query")
    @ResponseBody
    @CrossOrigin
    @AuthorizationRequired
    public ResponseEntity<String> doSql(HttpServletRequest req)
    throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = parser.parse(reader).getAsJsonObject();
            if (!json.has("query"))
                return errorResponse("malformed request", BAD_REQUEST);
            JsonObject res = queryRunner.runQuery(json.get("query").getAsString());
            return responseEntityJson(res.toString(), OK);
        } catch (DataAccessException e) {
            return errorResponse(e.getMessage(), BAD_REQUEST);
        }
    }

    public static ModelAndView getNotFound() {
        return NOT_FOUND;
    }
}
