package io.pivotal.identityService.samples.authcodeclientcredentials.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization tests locking in authcode-client-credentials' current HTTP contract
 * before the Spring Boot 4.1 migration. This app combines both grant types:
 * /user/todos requires an OIDC login, /client/todos is permitAll.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "RESOURCE_URL=http://localhost:9999",
        "ssoServiceUrl=http://uaa.example.com/uaa",
        "spring.security.oauth2.client.registration.ssoauthorizationcode.client-id=test-client",
        "spring.security.oauth2.client.registration.ssoauthorizationcode.client-secret=test-secret",
        "spring.security.oauth2.client.registration.ssoauthorizationcode.authorization-grant-type=authorization_code",
        "spring.security.oauth2.client.registration.ssoauthorizationcode.redirect-uri=http://localhost/login/oauth2/code/ssoauthorizationcode",
        "spring.security.oauth2.client.registration.ssoauthorizationcode.scope=openid",
        "spring.security.oauth2.client.provider.ssoauthorizationcode.authorization-uri=http://localhost/uaa/oauth/authorize",
        "spring.security.oauth2.client.provider.ssoauthorizationcode.token-uri=http://localhost/uaa/oauth/token",
        "spring.security.oauth2.client.provider.ssoauthorizationcode.user-info-uri=http://localhost/uaa/userinfo",
        "spring.security.oauth2.client.provider.ssoauthorizationcode.jwk-set-uri=http://localhost/uaa/token_keys",
        "spring.security.oauth2.client.registration.ssoclientcredentials.client-id=test-cc-client",
        "spring.security.oauth2.client.registration.ssoclientcredentials.client-secret=test-cc-secret",
        "spring.security.oauth2.client.registration.ssoclientcredentials.authorization-grant-type=client_credentials",
        "spring.security.oauth2.client.provider.ssoclientcredentials.token-uri=http://localhost/uaa/oauth/token",
})
class TodoControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    // IndexController/InfoController split tokens on "." and base64-decode segment [1],
    // so tokens must be JWT-shaped; Spring Security Test's placeholder values are not.
    private static String jwtWithClaims(String claimsJson) {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8))
                + "." + enc.encodeToString(claimsJson.getBytes(StandardCharsets.UTF_8))
                + ".signature";
    }

    private OAuth2AccessToken jwtAccessToken() {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                jwtWithClaims("{\"sub\":\"basic-user\",\"client_id\":\"sample-client-authcode-client-credentials\","
                        + "\"grant_type\":\"client_credentials\",\"scope\":[\"todo.read\",\"todo.write\"]}"),
                Instant.now(), Instant.now().plusSeconds(3600));
    }

    private OAuth2AuthenticationToken oidcAuthentication() {
        OidcIdToken idToken = new OidcIdToken(
                jwtWithClaims("{\"sub\":\"basic-user\",\"scope\":[\"openid\"]}"),
                Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("sub", "basic-user"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of(
                "sub", "basic-user", "name", "FirstName LastName", "email", "basic-user@example.com"));
        DefaultOidcUser principal = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, userInfo);
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "ssoauthorizationcode");
    }

    private Todo sampleTodo() {
        Todo todo = new Todo();
        todo.setId("abc-123");
        todo.setTask("seed-task-1");
        return todo;
    }

    @Test
    void userTodos_whenAuthenticated_rendersTodoList() throws Exception {
        when(todoService.getAll(any())).thenReturn(List.of(sampleTodo()));

        mockMvc.perform(get("/user/todos")
                        .with(authentication(oidcAuthentication()))
                        .with(oauth2Client("ssoauthorizationcode").accessToken(jwtAccessToken())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seed-task-1")));
    }

    @Test
    void userTodos_withoutAuthentication_redirects() throws Exception {
        mockMvc.perform(get("/user/todos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void userCreateTodo_whenResourceServerCallFails_propagatesSameStatusCode() throws Exception {
        when(todoService.create(any(), any())).thenThrow(
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, new byte[0], null));

        mockMvc.perform(post("/user/todos")
                        .with(authentication(oidcAuthentication()))
                        .with(oauth2Client("ssoauthorizationcode").accessToken(jwtAccessToken()))
                        .with(csrf())
                        .param("task", "new-task"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCreateTodo_success_redirectsToUserTodos() throws Exception {
        mockMvc.perform(post("/user/todos")
                        .with(authentication(oidcAuthentication()))
                        .with(oauth2Client("ssoauthorizationcode").accessToken(jwtAccessToken()))
                        .with(csrf())
                        .param("task", "new-task"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/todos"));
    }

    @Test
    void clientTodos_isPermitAll_andRendersTodoList() throws Exception {
        when(todoService.getAll(any())).thenReturn(List.of(sampleTodo()));

        mockMvc.perform(get("/client/todos")
                        .with(oauth2Client("ssoclientcredentials").accessToken(jwtAccessToken())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seed-task-1")));
    }

    @Test
    void index_isPermitAll_andRendersClientCredentialsToken() throws Exception {
        mockMvc.perform(get("/")
                        .with(oauth2Client("ssoclientcredentials").accessToken(jwtAccessToken())))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("sample-client-authcode-client-credentials")))
                // Jackson's default pretty-printer format -- pinned because Boot 4's
                // Jackson 3 default switch could silently change it. Quotes appear
                // HTML-escaped because index.html renders the token with th:text
                // (escaping) rather than th:utext.
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("&quot;grant_type&quot; : &quot;client_credentials&quot;")));
    }
}
