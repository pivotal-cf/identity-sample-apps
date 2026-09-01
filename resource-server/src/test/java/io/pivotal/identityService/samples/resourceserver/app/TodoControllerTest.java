package io.pivotal.identityService.samples.resourceserver.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization tests locking in resource-server's current JSON/HTTP contract
 * before the Spring Boot 4.1 migration. Any assertion failure after the migration
 * is a real behavioral regression to fix, not a test to relax.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-issuer.example.com")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ServerConfiguration#jwtDecoder() would otherwise make a real network call to the
    // (nonexistent, in tests) issuer at context startup; replacing it is required, not optional.
    // The jwt() request post-processor injects a pre-authenticated JWT directly and never
    // invokes this bean, so its behavior here doesn't matter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void list_withReadScope_returnsSeededTodosAsJson() throws Exception {
        mockMvc.perform(get("/todos").with(jwt().authorities(() -> "SCOPE_todo.read")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].task").value("seed-task-1"))
                .andExpect(jsonPath("$[1].task").value("seed-task-2"))
                .andExpect(jsonPath("$[2].task").value("seed-task-3"))
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0]", org.hamcrest.Matchers.aMapWithSize(2)));
    }

    @Test
    void list_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Bearer")));
    }

    @Test
    void list_withWrongScope_returns403() throws Exception {
        mockMvc.perform(get("/todos").with(jwt().authorities(() -> "SCOPE_todo.write")))
                .andExpect(status().isForbidden())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.containsString("insufficient_scope")));
    }

    // InMemoryTodoRepository is a singleton shared across all tests in this cached Spring
    // context; mutating tests must dirty the context so later tests see fresh seed data.
    //
    // Verified against the real (unmodified) app: although Todo has no setId(), Jackson's
    // default INFER_PROPERTY_MUTATORS falls back to direct private-field assignment because
    // getId() establishes "id" as a known property. A client-supplied id is honored, not
    // discarded, contrary to what reading the source alone would suggest.
    @Test
    @DirtiesContext
    void create_withWriteScope_returns201AndHonorsClientSuppliedId() throws Exception {
        String clientSuppliedId = "11111111-1111-1111-1111-111111111111";
        String body = "{\"id\":\"" + clientSuppliedId + "\",\"task\":\"new-task\"}";

        mockMvc.perform(post("/todos")
                        .with(jwt().authorities(() -> "SCOPE_todo.write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task").value("new-task"))
                .andExpect(jsonPath("$.id").value(clientSuppliedId));
    }

    @Test
    void create_withoutWriteScope_returns403() throws Exception {
        mockMvc.perform(post("/todos")
                        .with(jwt().authorities(() -> "SCOPE_todo.read"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\":\"new-task\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_unknownId_withWriteScope_returns404() throws Exception {
        mockMvc.perform(delete("/todos/22222222-2222-2222-2222-222222222222")
                        .with(jwt().authorities(() -> "SCOPE_todo.write")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    void createThenDelete_withWriteScope_returns204ThenRemovesItem() throws Exception {
        String createResponse = mockMvc.perform(post("/todos")
                        .with(jwt().authorities(() -> "SCOPE_todo.write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\":\"to-delete\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(delete("/todos/" + id)
                        .with(jwt().authorities(() -> "SCOPE_todo.write")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/todos").with(jwt().authorities(() -> "SCOPE_todo.read")))
                .andExpect(jsonPath("$[?(@.id=='" + id + "')]").isEmpty());
    }
}
