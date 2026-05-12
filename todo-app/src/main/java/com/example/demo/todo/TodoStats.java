package com.example.demo.todo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record TodoStats(
        int totalLists,
        long totalTasks,
        Map<TodoList.Priority, Long> tasksByPriority,
        BigDecimal completionRate,
        Instant generatedAt
) {}
