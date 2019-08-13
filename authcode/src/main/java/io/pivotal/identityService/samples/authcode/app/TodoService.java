package io.pivotal.identityService.samples.authcode.app;

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

    public List<Todo> getAll(String authorizationHeaderValue) {
        return this.webClient
                .get()
                .uri("/todos")
                .header("Authorization", authorizationHeaderValue)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Todo>>() {})
                .block();
    }

    public Todo create(String authorizationHeaderValue, TodoRequest todo) {
        return this.webClient
                .post()
                .uri("/todos")
                .header("Authorization", authorizationHeaderValue)
                .body(BodyInserters.fromObject(todo))
                .retrieve()
                .bodyToMono(Todo.class)
                .block();
    }

    public void delete(String authorizationHeaderValue, String id) {
        this.webClient
                .delete()
                .uri("/todos/" + id)
                .header("Authorization", authorizationHeaderValue)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
