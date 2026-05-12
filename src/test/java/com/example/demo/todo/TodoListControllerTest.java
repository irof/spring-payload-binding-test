package com.example.demo.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoListControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TodoListRepository repository;

    @Test
    void create_returnsNestedJson() throws Exception {
        var saved = new TodoList(
                "list-1",
                "リリース準備",
                new TodoList.Owner("山田", new TodoList.Contact("yamada@example.com", "090-1111-2222")),
                List.of(new TodoList.Task(
                        "task-1",
                        "設計レビュー",
                        TodoList.Priority.HIGH,
                        List.of(new TodoList.Subtask("要件確認", false),
                                new TodoList.Subtask("図の作成", true))))
        );
        when(repository.save(any())).thenReturn(saved);

        var request = """
                {
                  "title": "リリース準備",
                  "owner": {
                    "name": "山田",
                    "contact": {"email": "yamada@example.com", "phone": "090-1111-2222"}
                  },
                  "tasks": [
                    {
                      "title": "設計レビュー",
                      "priority": "HIGH",
                      "subtasks": [
                        {"title": "要件確認", "done": false},
                        {"title": "図の作成", "done": true}
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/todo-lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("list-1"))
                .andExpect(jsonPath("$.owner.contact.email").value("yamada@example.com"))
                .andExpect(jsonPath("$.tasks[0].subtasks[1].done").value(true));
    }

    @Test
    void get_returnsNotFound() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        mockMvc.perform(get("/todo-lists/missing")).andExpect(status().isNotFound());
    }
}
