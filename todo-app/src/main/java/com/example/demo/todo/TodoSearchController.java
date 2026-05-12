package com.example.demo.todo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TodoSearchController {

    private final TodoListRepository repository;

    public TodoSearchController(TodoListRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/todo-lists/search")
    public SearchResult search(@RequestBody SearchCriteria criteria) {
        List<TodoList> items = repository.findAll().stream()
                .filter(t -> criteria.keyword() == null || t.title().contains(criteria.keyword()))
                .toList();
        int size = criteria.page() != null ? criteria.page().size() : items.size();
        int pages = size == 0 ? 0 : (items.size() + size - 1) / size;
        return new SearchResult(items, new SearchResult.PageInfo(0, size, pages), items.size());
    }
}
