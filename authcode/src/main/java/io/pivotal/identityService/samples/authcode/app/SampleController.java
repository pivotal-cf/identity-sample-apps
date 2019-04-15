package io.pivotal.identityService.samples.authcode.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
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
public class SampleController {
    @Value("${ssoServiceUrl:placeholder}")
    String ssoServiceUrl;

    private ObjectMapper objectMapper;

    public SampleController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/authorization_code")
    public String authorizationCode(
            Model model,
            OAuth2AuthenticationToken authentication,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient) throws Exception {
        // Check if app has been bound to SSO
        if (ssoServiceUrl.equals("placeholder")) {
            model.addAttribute("header", "Warning: You need to bind to the SSO service.");
            model.addAttribute("warning", "Please bind your app to restore regular functionality");
            return "configure_warning";
        }

        // Display user information
        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
        model.addAttribute("ssoServiceUrl", ssoServiceUrl);

        OidcUserInfo userInfo = defaultOidcUser.getUserInfo();
        if (userInfo != null) {
            model.addAttribute("user_info", toPrettyJsonString(userInfo.getClaims()));
        }

        OidcIdToken idToken = defaultOidcUser.getIdToken();
        model.addAttribute("id_token", toPrettyJsonString(parseToken(idToken.getTokenValue())));

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        if (accessToken != null) {
            String accessTokenValue = accessToken.getTokenValue();
            model.addAttribute("access_token", toPrettyJsonString(parseToken(accessTokenValue)));
        }

        return "authorization_code";
    }

    private Map<String, ?> parseToken(String base64Token) throws IOException {
        String token = base64Token.split("\\.")[1];
        return objectMapper.readValue(Base64.decodeBase64(token), new TypeReference<Map<String, ?>>() {
        });
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
