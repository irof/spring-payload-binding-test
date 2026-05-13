package com.example.demo.todo;

import java.util.List;

public record SearchResult(
        List<TodoList> items,
        PageInfo page,
        long totalCount
) {
    public record PageInfo(int number, int size, int totalPages) {}
}
