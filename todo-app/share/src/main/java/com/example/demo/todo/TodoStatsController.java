package com.example.demo.todo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

@RestController
public class TodoStatsController {

    private final TodoListRepository repository;

    public TodoStatsController(TodoListRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/todo-lists/stats")
    public TodoStats stats() {
        var lists = repository.findAll();
        var allTasks = lists.stream().flatMap(t -> t.tasks().stream()).toList();
        long total = allTasks.size();
        long done = allTasks.stream()
                .filter(t -> t.subtasks().stream().allMatch(TodoList.Subtask::done))
                .count();

        Map<TodoList.Priority, Long> byPriority = new EnumMap<>(TodoList.Priority.class);
        for (TodoList.Priority p : TodoList.Priority.values()) byPriority.put(p, 0L);
        allTasks.forEach(t -> byPriority.merge(t.priority(), 1L, Long::sum));

        BigDecimal rate = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(done).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return new TodoStats(lists.size(), total, byPriority, rate, Instant.now());
    }
}
