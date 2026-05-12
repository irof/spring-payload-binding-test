package com.example.demo.todo;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TodoListRepository {

    private final ConcurrentMap<String, TodoList> store = new ConcurrentHashMap<>();

    public Collection<TodoList> findAll() {
        return store.values();
    }

    public Optional<TodoList> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public TodoList save(TodoList todoList) {
        String id = todoList.id() != null ? todoList.id() : UUID.randomUUID().toString();
        var tasksWithIds = todoList.tasks() == null ? List.<TodoList.Task>of()
                : todoList.tasks().stream()
                .map(t -> new TodoList.Task(
                        t.id() != null ? t.id() : UUID.randomUUID().toString(),
                        t.title(),
                        t.priority(),
                        t.subtasks() == null ? List.of() : t.subtasks()))
                .toList();
        var saved = new TodoList(id, todoList.title(), todoList.owner(), tasksWithIds);
        store.put(id, saved);
        return saved;
    }

    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }
}
