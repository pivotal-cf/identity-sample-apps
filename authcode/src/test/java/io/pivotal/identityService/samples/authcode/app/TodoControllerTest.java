package io.pivotal.identityService.samples.authcode.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization tests locking in authcode's current HTTP contract before the
 * Spring Boot 4.1 migration. Any assertion failure after the migration is a real
 * behavioral regression to fix, not a test to relax.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "RESOURCE_URL=http://localhost:9999",
        "spring.security.oauth2.client.registration.sso.client-id=test-client",
        "spring.security.oauth2.client.registration.sso.client-secret=test-secret",
        "spring.security.oauth2.client.registration.sso.authorization-grant-type=authorization_code",
        "spring.security.oauth2.client.registration.sso.redirect-uri=http://localhost/login/oauth2/code/sso",
        "spring.security.oauth2.client.registration.sso.scope=openid",
        "spring.security.oauth2.client.provider.sso.authorization-uri=http://localhost/uaa/oauth/authorize",
        "spring.security.oauth2.client.provider.sso.token-uri=http://localhost/uaa/oauth/token",
        "spring.security.oauth2.client.provider.sso.user-info-uri=http://localhost/uaa/userinfo",
        "spring.security.oauth2.client.provider.sso.jwk-set-uri=http://localhost/uaa/token_keys",
})
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    @Test
    void todos_whenAuthenticated_rendersTodoListFromService() throws Exception {
        Todo todo = new Todo();
        todo.setId("abc-123");
        todo.setTask("seed-task-1");
        when(todoService.getAll()).thenReturn(List.of(todo));

        mockMvc.perform(get("/todos").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seed-task-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("abc-123")));
    }

    @Test
    void todos_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void todos_whenResourceServerCallFails_rendersErrorInsteadOfFailingPage() throws Exception {
        when(todoService.getAll()).thenThrow(
                WebClientResponseException.create(403, "Forbidden", HttpHeaders.EMPTY, new byte[0], null));

        mockMvc.perform(get("/todos").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("403")));
    }

    @Test
    void createTodo_success_redirectsToTodos() throws Exception {
        mockMvc.perform(post("/todos")
                        .with(oidcLogin())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("task", "new-task"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));
    }

    @Test
    void createTodo_whenResourceServerCallFails_propagatesSameStatusCode() throws Exception {
        when(todoService.create(any())).thenThrow(
                WebClientResponseException.create(400, "Bad Request", HttpHeaders.EMPTY, new byte[0], null));

        mockMvc.perform(post("/todos")
                        .with(oidcLogin())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("task", "new-task"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTodo_success_redirectsToTodos() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/todos/abc-123")
                        .with(oidcLogin())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/todos"));
    }

    @Test
    void deleteTodo_whenResourceServerCallFails_propagatesSameStatusCode() throws Exception {
        org.mockito.Mockito.doThrow(
                WebClientResponseException.create(404, "Not Found", HttpHeaders.EMPTY, new byte[0], null))
                .when(todoService).delete(anyString());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/todos/unknown-id")
                        .with(oidcLogin())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }
}
