package com.example.demo.todo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/todo-lists")
public class TodoListController {

    private final TodoListRepository repository;

    public TodoListController(TodoListRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Collection<TodoList> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoList> get(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TodoList> create(@RequestBody TodoList body) {
        var created = repository.save(new TodoList(null, body.title(), body.owner(), body.tasks()));
        return ResponseEntity.created(URI.create("/todo-lists/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoList> update(@PathVariable String id, @RequestBody TodoList body) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var saved = repository.save(new TodoList(id, body.title(), body.owner(), body.tasks()));
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return repository.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
