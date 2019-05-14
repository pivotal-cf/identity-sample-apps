package io.pivotal.identityService.samples.clientcredentials.app;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class TodoService {
    private WebClient webClient;

    public TodoService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Todo> getAll() {
        return this.webClient
                .get()
                .uri("/todos")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Todo>>() {})
                .block();
    }

    public Todo create(TodoRequest todo) {
        return this.webClient
                .post()
                .uri("/todos")
                .body(BodyInserters.fromObject(todo))
                .retrieve()
                .bodyToMono(Todo.class)
                .block();
    }

    public void delete(String id) {
        this.webClient
                .delete()
                .uri("/todos/" + id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
