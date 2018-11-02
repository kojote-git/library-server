package com.jkojote.libraryserver.application.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jkojote.library.domain.model.book.Book;
import com.jkojote.library.domain.model.book.instance.BookFormat;
import com.jkojote.library.domain.model.book.instance.BookInstance;
import com.jkojote.library.domain.model.book.instance.isbn.Isbn13;
import com.jkojote.library.domain.shared.domain.DomainRepository;
import com.jkojote.library.files.FileInstance;
import com.jkojote.libraryserver.application.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

import static com.jkojote.libraryserver.application.controllers.Util.errorResponse;
import static com.jkojote.libraryserver.application.controllers.Util.responseMessage;

@RestController
@RequestMapping("instances")
public class BookInstanceController {

    private DomainRepository<BookInstance> bookInstanceRepository;

    private DomainRepository<Book> bookRepository;

    private JsonConverter<BookInstance> biJsonConverter;

    private JsonParser jsonParser;

    @Autowired
    public BookInstanceController(@Qualifier("bookInstanceRepository")
                                  DomainRepository<BookInstance> bookInstanceRepository,
                                  @Qualifier("biJsonConverter")
                                  JsonConverter<BookInstance> bookInstanceJsonConverter,
                                  @Qualifier("bookRepository")
                                  DomainRepository<Book> bookRepository) {
        this.bookRepository = bookRepository;
        this.bookInstanceRepository = bookInstanceRepository;
        this.biJsonConverter = bookInstanceJsonConverter;
        this.jsonParser = new JsonParser();
    }

    @GetMapping("{id}")
    @CrossOrigin
    public ResponseEntity<String> getBookInstance(@PathVariable("id") long id) {
        BookInstance bi = bookInstanceRepository.findById(id);
        if (bi == null)
            return errorResponse("no such book with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        return new ResponseEntity<>(biJsonConverter.convertToString(bi), HttpStatus.OK);
    }

    @GetMapping("{id}/file")
    @CrossOrigin
    public void getBookInstanceFile(@PathVariable("id") long id, HttpServletResponse resp)
    throws IOException {
        try (OutputStream out = resp.getOutputStream()) {
            BookInstance bi = bookInstanceRepository.findById(id);
            if (bi == null) {
                resp.setStatus(422);
                out.write(("no such bookInstance with id" + id).getBytes());
                out.flush();
                return;
            }
            out.write(bi.getFile().asBytes());
            out.flush();
        }
    }

    @PostMapping("creation")
    @CrossOrigin
    public ResponseEntity<String> creation(ServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            long bookId = json.get("bookId").getAsLong();
            Book book = bookRepository.findById(bookId);
            if (book == null)
                return errorResponse("no such book with id: "+bookId, HttpStatus.UNPROCESSABLE_ENTITY);
            Isbn13 isbn13 = Isbn13.of(json.get("isbn13").getAsString());
            BookFormat format = BookFormat.of(json.get("format").getAsString());
            long id = bookInstanceRepository.nextId();
            BookInstance bookInstance = new BookInstance(id, book, isbn13, format);
            bookInstanceRepository.save(bookInstance);
            return new ResponseEntity<>("{\"id\":"+id+"}", HttpStatus.CREATED);
        }
    }

    @PutMapping("{id}/file")
    @CrossOrigin
    public ResponseEntity<String> fileUpdating(@PathVariable("id") long id, HttpServletRequest req)
    throws IOException, ServletException {
        BookInstance bi = bookInstanceRepository.findById(id);
        if (bi == null)
            return errorResponse("no such book instance with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        for (Part p : req.getParts()) {
            try (InputStream in = p.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                byte[] file = out.toByteArray();
                bi.setFile(new FromBytesFileInstance(file));
                bookInstanceRepository.update(bi);
                break;
            }
        }
        return responseMessage("book instance has been updated", HttpStatus.OK);
    }

    @PutMapping("{id}/editing")
    @CrossOrigin
    public ResponseEntity<String> editing(@PathVariable("id") long id, ServletRequest req)
    throws IOException {
        BookInstance bi = bookInstanceRepository.findById(id);
        if (bi == null)
            return errorResponse("no such book with id: "+id, HttpStatus.UNPROCESSABLE_ENTITY);
        try (BufferedReader reader = req.getReader()) {
            JsonObject json = jsonParser.parse(reader).getAsJsonObject();
            BookFormat format = BookFormat.of(json.get("format").getAsString());
            bi.setFormat(format);
            bookInstanceRepository.update(bi);
        }
        return responseMessage("book instance has been successfully updated", HttpStatus.OK);
    }

    private class FromBytesFileInstance implements FileInstance {

        private byte[] bytes;

        public FromBytesFileInstance(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public byte[] asBytes() {
            return bytes;
        }

        @Override
        public byte[] asBytes(int i) {
            return Arrays.copyOf(bytes, i);
        }

        @Override
        public int length() {
            return bytes.length;
        }

        @Override
        public Blob asBlob() {
            try {
                Blob b = new SerialBlob(bytes);
                return b;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
