package io.pivotal.identityService.samples.authcodeclientcredentials.app;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Component
public class TodoService {
    private WebClient webClient;

    public TodoService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Todo> getAll(OAuth2AuthorizedClient authorizedClient) {
        return this.webClient
                .get()
                .uri("/todos")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Todo>>() {})
                .block();
    }

    public Todo create(TodoRequest todo, OAuth2AuthorizedClient authorizedClient) {
        return this.webClient
                .post()
                .uri("/todos")
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .body(BodyInserters.fromObject(todo))
                .retrieve()
                .bodyToMono(Todo.class)
                .block();
    }

    public void delete(String id, OAuth2AuthorizedClient authorizedClient) {
        this.webClient
                .delete()
                .uri("/todos/" + id)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
