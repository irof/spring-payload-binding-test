package com.example.demo.todo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ListIdEchoController {

    public record ListId(@JsonValue UUID value) {
        @JsonCreator
        public static ListId from(String id) {
            return new ListId(UUID.fromString(id));
        }
    }

    @PostMapping("/ids/echo")
    public ListId echo(@RequestBody ListId id) {
        return id;
    }
}
