package io.pivotal.identityService.samples.authcodeclientcredentials.app;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Map;

@Controller
public class InfoController {
    @Value("${ssoServiceUrl:placeholder}")
    String ssoServiceUrl;

    private ObjectMapper objectMapper;

    public InfoController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/info")
    public String info(
            Model model,
            OAuth2AuthenticationToken authentication,
            @RegisteredOAuth2AuthorizedClient("ssoauthorizationcode") OAuth2AuthorizedClient authcodeClient,
            @RegisteredOAuth2AuthorizedClient("ssoclientcredentials") OAuth2AuthorizedClient clientCredentialsClient) throws Exception {
        // Check if app has been bound to SSO
        if (ssoServiceUrl.equals("placeholder")) {
            model.addAttribute("header", "Warning: You need to bind to the SSO service.");
            model.addAttribute("warning", "Please bind your app to restore regular functionality");
            return "configure_warning";
        }

        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
        model.addAttribute("ssoServiceUrl", ssoServiceUrl);

        OidcUserInfo userInfo = defaultOidcUser.getUserInfo();
        if (userInfo != null) {
            model.addAttribute("userInfo", toPrettyJsonString(userInfo.getClaims()));
        }

        OidcIdToken idToken = defaultOidcUser.getIdToken();
        model.addAttribute("idToken", toPrettyJsonString(parseToken(idToken.getTokenValue())));

        OAuth2AccessToken authcodeAccessToken = authcodeClient.getAccessToken();
        if (authcodeAccessToken != null) {
            String accessTokenValue = authcodeAccessToken.getTokenValue();
            model.addAttribute("authcodeAccessToken", toPrettyJsonString(parseToken(accessTokenValue)));
        }

        OAuth2AccessToken clientCredentialsToken = clientCredentialsClient.getAccessToken();
        if (clientCredentialsToken != null) {
            String accessTokenValue = clientCredentialsToken.getTokenValue();
            model.addAttribute("clientCredentialsToken", toPrettyJsonString(parseToken(accessTokenValue)));
        }

        return "info";
    }

    private Map<String, ?> parseToken(String base64Token) throws IOException {
        String token = base64Token.split("\\.")[1];
        return objectMapper.readValue(java.util.Base64.getDecoder().decode(token), new TypeReference<Map<String, ?>>() {
        });
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
