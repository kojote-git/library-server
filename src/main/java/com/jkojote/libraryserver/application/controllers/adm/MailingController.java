package com.jkojote.libraryserver.application.controllers.adm;

import com.jkojote.library.domain.model.reader.Reader;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.libraryserver.application.controllers.utils.Context;
import com.jkojote.libraryserver.application.mailing.MailSender;
import com.jkojote.libraryserver.application.mailing.MessageData;
import com.jkojote.libraryserver.application.mailing.PlainMessageData;
import com.jkojote.libraryserver.application.mailing.PropertiesAuthenticator;
import com.jkojote.libraryserver.application.recomendations.Recommendation;
import com.jkojote.libraryserver.application.recomendations.RecommendationsGenerator;
import com.jkojote.libraryserver.application.security.AuthorizationRequired;
import com.jkojote.types.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.errorResponse;
import static com.jkojote.libraryserver.application.controllers.utils.ControllerUtils.responseMessage;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/adm/mailing")
public class MailingController {

    private DomainRepository<Reader> readerRepository;

    private RecommendationsGenerator generator;

    private MailSender mailSender;

    private ITemplateEngine templateEngine;

    private AtomicBoolean recommendationsBeingGenerated;

    private Authenticator authenticator;

    @Autowired
    public MailingController(
            @Qualifier("readerRepository")
            DomainRepository<Reader> readerRepository,
            RecommendationsGenerator generator,
            MailSender mailSender,
            ITemplateEngine templateEngine) throws IOException {
        this.readerRepository = readerRepository;
        this.generator = generator;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.recommendationsBeingGenerated = new AtomicBoolean(false);
        this.authenticator = new PropertiesAuthenticator("/home/isaac/Desktop/mailing.properties");
    }

    @PostMapping("")
    @AuthorizationRequired
    public ResponseEntity<String> mailing(HttpServletRequest req) {
        if (recommendationsBeingGenerated.get())
            return responseMessage("recommendations are being generated", OK);
        try {
            recommendationsBeingGenerated.set(true);
            for (Reader r : readerRepository.findAll()) {
                List<Recommendation> recommendations = generator.getFor(r, 5);
                IContext ctx = Context.builder()
                        .add("recommendations", recommendations).build();
                String html = templateEngine.process("recommendations-list", ctx);
                MessageData data = PlainMessageData.Builder.create(false)
                        .addRecipient(toAddress(r.getEmail()))
                        .setMimeType("text/html")
                        .setContent(html)
                        .setSubject("Here is the recommendations")
                        .build();
                mailSender.send(data, authenticator);
            }
            recommendationsBeingGenerated.set(false);
            return responseMessage("recommendations are generated", OK);
        } catch (Exception e) {
            recommendationsBeingGenerated.set(false);
            return errorResponse("something went wrong", INTERNAL_SERVER_ERROR);
        }
    }

    private Address toAddress(Email email) {
        try {
            return new InternetAddress(email.toString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
