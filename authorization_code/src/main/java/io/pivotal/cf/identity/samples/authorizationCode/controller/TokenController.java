package io.pivotal.cf.identity.samples.authorizationCode.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
public class TokenController {

    @Autowired
    WebClient webClient;

    @Value("${resourceServerUrl}")
    private String resourceServerUrl;

    @RequestMapping(value = "/")
    public String home() {

        return "<html>\n" +
                "<body>\n" +
                "<a href=\"/app\">Login</a>\n" + "<br/>" +
                "</body>\n" +
                "</html>";
    }

    @RequestMapping(value = "/app")
    public String app(@RegisteredOAuth2AuthorizedClient("sso") OAuth2AuthorizedClient oauth2client, @AuthenticationPrincipal OidcUser oidcUser) {

        return "<html>\n" +
                "<body>\n" +
                "Access Token: " + oauth2client.getAccessToken().getTokenValue() + "<br/>" +
                displayAccessToken(oauth2client.getAccessToken().getTokenValue()) + "<br/>" +
                "ID Token: " + oidcUser.getIdToken().getTokenValue() + "<br/>" +
                displayIdToken(oidcUser.getIdToken().getTokenValue().toString()) + "<br/>" +
                "User Info Endpoint: " + oauth2client.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri() + "<br/>" +
                displayUserInfo(oidcUser.getUserInfo().getClaims()) + "<br/>" + "\n" +
                "<a href=\"/api\">Resource Server</a>\n" + "<br/>" +
                "<a href=\"/logout\">Logout</a>\n" + "<br/>" +
                "</body>\n" +
                "</html>";
    }

    @RequestMapping(value = "/api")
    public String getResourceServerData(@RegisteredOAuth2AuthorizedClient("sso") OAuth2AuthorizedClient oauth2client) {

        String response = this.webClient
                .get()
                .uri(resourceServerUrl)
                .attributes(oauth2AuthorizedClient(oauth2client))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return "<html>\n" +
                "<body>\n" +
                "Get Response: " + response + "<br/>" +
                "<br/>" +
                "<a href=\"/api/post\">Post</a>\n" + "<br/>" +
                "<a href=\"/app\">Back</a>\n" + "<br/>" +
                "</body>\n" +
                "</html>";
    }

    @RequestMapping(value = "/api/post")
    public void postResourceServerData(HttpServletResponse response, @RegisteredOAuth2AuthorizedClient("sso") OAuth2AuthorizedClient oauth2client) throws IOException {
        this.webClient
                .post()
                .uri(resourceServerUrl)
                .attributes(oauth2AuthorizedClient(oauth2client))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        response.sendRedirect("/api");
    }

    private String displayAccessToken(String token) {
        return "Access Token:" +
                "\n" +
                "<pre>" +  prettyPrintJwtBody(token) + "</pre>\n";
    }

    private String displayIdToken(String token) {
        return "ID Token:" +
                "\n" +
                "<pre>" +  prettyPrintJwtBody(token) + "</pre>\n";
    }

    private String displayUserInfo(Map<String,Object> userInfo) {
        String userInfoJson;
        try {
            userInfoJson = new ObjectMapper().writeValueAsString(userInfo);
        } catch (JsonProcessingException e) {
            return "Error parsing User Info response";
        }
        return "User Info:" +
                "\n" +
                "<pre>" +  prettyPrintJSON(userInfoJson) + "</pre>\n";
    }

    private String prettyPrintJwtBody(String jwtToken)  {
            String[] split_string = jwtToken.split("\\.");
            try {
                String base64EncodedBody = split_string[1];
                byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedBody);
                return prettyPrintJSON(new String(decodedBytes));
            } catch (ArrayIndexOutOfBoundsException e) {
                return "Error parsing JWT token";
            }
    }

    private String prettyPrintJSON(String json)  {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(json,Object.class));
        } catch (JsonProcessingException e) {
            return "Error parsing JSON";
        } catch (IOException e) {
            return "Error parsing token";
        }
    }

}
