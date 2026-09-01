package io.pivotal.identityService.samples.authcode.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization tests for authcode's /info page.
 *
 * High value for the Spring Boot 4.1 migration specifically: this page renders JWT
 * claims via ObjectMapper#writerWithDefaultPrettyPrinter(), so it pins both the
 * ObjectMapper bean's availability and its exact pretty-printed output format --
 * the two things most at risk from Boot 4's Jackson 2 -> Jackson 3 default switch.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "RESOURCE_URL=http://localhost:9999",
        "ssoServiceUrl=http://uaa.example.com/uaa",
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
class InfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // InfoController#parseToken splits the token on "." and base64url-decodes segment [1],
    // so tokens must be JWT-shaped. Spring Security Test's oidcLogin() supplies non-JWT
    // placeholder values ("id-token"/"access-token") and cannot be used here.
    private static String jwtWithClaims(String claimsJson) {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String header = enc.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = enc.encodeToString(claimsJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    private static final String ID_TOKEN_VALUE =
            jwtWithClaims("{\"sub\":\"basic-user\",\"scope\":[\"openid\"],\"iss\":\"http://uaa.example.com/uaa\"}");
    private static final String ACCESS_TOKEN_VALUE =
            jwtWithClaims("{\"sub\":\"basic-user\",\"user_name\":\"basic-user\","
                    + "\"email\":\"basic-user@example.com\",\"client_id\":\"sample-client-authcode\","
                    + "\"grant_type\":\"authorization_code\",\"scope\":[\"openid\"]}");

    private OAuth2AuthenticationToken oidcAuthentication() {
        OidcIdToken idToken = new OidcIdToken(ID_TOKEN_VALUE, Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("sub", "basic-user", "iss", "http://uaa.example.com/uaa"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of(
                "sub", "basic-user",
                "user_name", "basic-user",
                "name", "FirstName LastName",
                "email", "basic-user@example.com"));
        DefaultOidcUser principal = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, userInfo);
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "sso");
    }

    private OAuth2AccessToken jwtAccessToken() {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, ACCESS_TOKEN_VALUE,
                Instant.now(), Instant.now().plusSeconds(3600));
    }

    @Test
    void info_whenAuthenticated_rendersUserInfoIdTokenAndAccessTokenAsPrettyJson() throws Exception {
        mockMvc.perform(get("/info")
                        .with(authentication(oidcAuthentication()))
                        .with(oauth2Client("sso").accessToken(jwtAccessToken())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"user_info\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"access_token\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"id_token\"")))
                // claims decoded out of the access token JWT and rendered into the page
                .andExpect(content().string(org.hamcrest.Matchers.containsString("basic-user@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sample-client-authcode")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("authorization_code")))
                // userinfo claims
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FirstName LastName")))
                // Jackson's default pretty-printer format: 2-space indent, space before colon.
                // This is exactly what Boot 4's Jackson 3 default switch could silently change.
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"email\" : \"basic-user@example.com\"")));
    }

    @Test
    void info_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().is3xxRedirection());
    }
}
