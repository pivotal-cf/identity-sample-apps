package io.pivotal.identityService.samples.clientcredentials.app;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization tests locking in client-credentials' current HTTP contract before
 * the Spring Boot 4.1 migration. Unlike authcode, this app's security is permitAll()
 * -- no user login -- so /todos is reachable anonymously.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "RESOURCE_URL=http://localhost:9999",
        "spring.security.oauth2.client.registration.sso.client-id=test-client",
        "spring.security.oauth2.client.registration.sso.client-secret=test-secret",
        "spring.security.oauth2.client.registration.sso.authorization-grant-type=client_credentials",
        "spring.security.oauth2.client.provider.sso.token-uri=http://localhost/uaa/oauth/token",
})
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    @Test
    void todos_anonymously_rendersTodoList() throws Exception {
        Todo todo = new Todo();
        todo.setId("abc-123");
        todo.setTask("seed-task-1");
        when(todoService.getAll()).thenReturn(List.of(todo));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seed-task-1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("abc-123")));
    }

    @Test
    void todos_whenResourceServerCallFails_rendersErrorInsteadOfFailingPage() throws Exception {
        when(todoService.getAll()).thenThrow(
                WebClientResponseException.create(403, "Forbidden", HttpHeaders.EMPTY, new byte[0], null));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("403")));
    }

    /**
     * Pins a real, pre-existing quirk (NOT introduced by the Boot 4.1 migration):
     * client-credentials/src/main/resources/application.yml is the only one of the three
     * UI apps that does not set spring.mvc.hiddenmethod.filter.enabled=true, so the
     * delete form's POST + hidden _method=DELETE is never translated to a DELETE and
     * hits no handler. Verified empirically against the running app (405, Allow: DELETE).
     *
     * The existing ClientCredentialsTest journey appears to pass over this because it
     * asserts only that the deleted item's text is absent from the page -- which is also
     * true of the resulting error page. Characterized as-is per the "behavior must not
     * change" requirement; fixing it would be a separate, deliberate decision.
     */
    @Test
    void deleteTodoViaHiddenMethodField_isNotTranslatedToDelete() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/todos/abc-123")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("_method", "DELETE"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void todosPage_includesCsrfTokenAndDeleteFormWithHiddenMethodField() throws Exception {
        Todo todo = new Todo();
        todo.setId("abc-123");
        todo.setTask("seed-task-1");
        when(todoService.getAll()).thenReturn(List.of(todo));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                // Spring Security's RequestDataValueProcessor auto-injects _csrf into every form
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_method\" value=\"DELETE\"")));
    }
}
