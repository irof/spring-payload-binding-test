package com.example.demo.todo;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
public class TodoExportController {

    @GetMapping(value = "/todo-lists/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Resource export() {
        return new ByteArrayResource("placeholder".getBytes(StandardCharsets.UTF_8));
    }
}
