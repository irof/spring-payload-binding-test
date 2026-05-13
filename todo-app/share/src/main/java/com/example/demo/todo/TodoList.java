package com.example.demo.todo;

import java.util.List;

public record TodoList(
        String id,
        String title,
        Owner owner,
        List<Task> tasks
) {
    public record Owner(String name, Contact contact) {}

    public record Contact(String email, String phone) {}

    public record Task(String id, String title, Priority priority, List<Subtask> subtasks) {}

    public record Subtask(String title, boolean done) {}

    public enum Priority { LOW, MEDIUM, HIGH }
}
