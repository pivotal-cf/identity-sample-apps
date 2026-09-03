package io.pivotal.identityService.samples.authcode.app;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TodoService {
    private final RestClient restClient;

    public TodoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Todo> getAll() {
        return this.restClient
                .get()
                .uri("/todos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Todo>>() {});
    }

    public Todo create(TodoRequest todo) {
        return this.restClient
                .post()
                .uri("/todos")
                .body(todo)
                .retrieve()
                .body(Todo.class);
    }

    public void delete(String id) {
        this.restClient
                .delete()
                .uri("/todos/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
