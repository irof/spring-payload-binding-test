package com.example.demo.todo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todo-lists/{listId}/comments")
public class CommentController {

    @PostMapping
    public Comment add(@PathVariable String listId, @RequestBody Comment comment) {
        return new Comment(
                UUID.randomUUID().toString(),
                comment.body(),
                comment.author(),
                LocalDateTime.now());
    }

    @GetMapping
    public List<Comment> list(@PathVariable String listId) {
        return List.of();
    }
}
