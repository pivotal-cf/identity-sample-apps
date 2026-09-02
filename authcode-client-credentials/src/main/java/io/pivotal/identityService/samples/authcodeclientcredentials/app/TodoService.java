package io.pivotal.identityService.samples.authcodeclientcredentials.app;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TodoService {
    private final RestClient restClient;

    public TodoService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Todo> getAll(OAuth2AuthorizedClient authorizedClient) {
        return this.restClient
                .get()
                .uri("/todos")
                .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Todo>>() {});
    }

    public Todo create(TodoRequest todo, OAuth2AuthorizedClient authorizedClient) {
        return this.restClient
                .post()
                .uri("/todos")
                .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                .body(todo)
                .retrieve()
                .body(Todo.class);
    }

    public void delete(String id, OAuth2AuthorizedClient authorizedClient) {
        this.restClient
                .delete()
                .uri("/todos/" + id)
                .headers(headers -> headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue()))
                .retrieve()
                .toBodilessEntity();
    }
}
