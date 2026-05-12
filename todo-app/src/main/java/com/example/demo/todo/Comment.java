package com.example.demo.todo;

import java.time.LocalDateTime;

public record Comment(
        String id,
        String body,
        Author author,
        LocalDateTime postedAt
) {
    public record Author(String name, String email) {}
}
