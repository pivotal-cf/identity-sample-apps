package org.cloudfoundry.identity.samples.authcode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.codec.binary.Base64;
import org.cloudfoundry.identity.oauth2.composite.CompositeAccessTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Configuration
@EnableAutoConfiguration
@ComponentScan
@Controller
@EnableOAuth2Sso
public class Application {

    public static void main(String[] args) {
        if ("true".equals(System.getenv("SKIP_SSL_VALIDATION"))) {
            SSLValidationDisabler.disableSSLValidation();
        }
        SpringApplication.run(Application.class, args);
    }

    // property set by spring-cloud-sso-connector
    @Value("${ssoServiceUrl:http:localhost:8080/uaa}")
    private String ssoServiceUrl;

    @Autowired(required = false)
    private OAuth2RestTemplate oauth2RestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        oauth2RestTemplate.setAccessTokenProvider(accessTokenProviderChain());
    }

    @Bean
    public AccessTokenProvider accessTokenProviderChain() {
        return new AccessTokenProviderChain(Arrays.<AccessTokenProvider> asList(
            new CompositeAccessTokenProvider(),
            new AuthorizationCodeAccessTokenProvider(), new ImplicitAccessTokenProvider(),
            new ResourceOwnerPasswordAccessTokenProvider(), new ClientCredentialsAccessTokenProvider()));
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/authorization_code")
    public String authCode(Model model, HttpSession session) throws Exception {
        if (oauth2RestTemplate == null) {
            return "configure_warning";
        }
        Map<?,?> userInfoResponse = oauth2RestTemplate.getForObject("{ssoServiceUrl}/userinfo", Map.class,
                ssoServiceUrl);
        model.addAttribute("ssoServiceUrl",ssoServiceUrl);
        model.addAttribute("response",toPrettyJsonString(userInfoResponse));
        Map<String, ?> token = getToken(oauth2RestTemplate.getOAuth2ClientContext());
        model.addAttribute("token",toPrettyJsonString(token));
        model.addAttribute("id_token", toPrettyJsonString(parseToken((String) session.getAttribute("openid_token"))));
        return "authorization_code";
    }

    private Map<String, ?> parseToken(String base64Token) throws IOException {
        String token = base64Token.split("\\.")[1];
        return objectMapper.readValue(Base64.decodeBase64(token), new TypeReference<Map<String, ?>>() {
        });
    }

    private Map<String, ?> getToken(OAuth2ClientContext clientContext) throws Exception {
        if (clientContext.getAccessToken() != null) {
            return parseToken(clientContext.getAccessToken().getValue());
        }
        return null;
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @RequestMapping(value = "/save_id_token", method = GET)
    public ResponseEntity<Void> saveIdToken(@RequestParam(value = "id_token") String idToken, HttpSession session) {
        session.setAttribute("openid_token", idToken);
        return new ResponseEntity<Void>(CREATED);
    }

}
