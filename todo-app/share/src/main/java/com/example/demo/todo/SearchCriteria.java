package com.example.demo.todo;

import java.time.LocalDate;
import java.util.List;

public record SearchCriteria(
        String keyword,
        Filter filter,
        Page page
) {
    public record Filter(
            LocalDate from,
            LocalDate to,
            List<TodoList.Priority> priorities,
            boolean includeCompleted
    ) {}

    public record Page(int number, int size) {}
}
