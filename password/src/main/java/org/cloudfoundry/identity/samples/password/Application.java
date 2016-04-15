package org.cloudfoundry.identity.samples.password;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.cloudfoundry.identity.samples.oauth2.composite.CompositeAccessTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Controller
public class Application {

    public static void main(String[] args) {
        if ("true".equals(System.getenv("SKIP_SSL_VALIDATION"))) {
            org.cloudfoundry.identity.samples.password.SSLValidationDisabler.disableSSLValidation();
        }
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        oAuth2RestTemplate.setAccessTokenProvider(accessTokenProviderChain());
    }

    @Bean
    public AccessTokenProvider accessTokenProviderChain() {
        return new AccessTokenProviderChain(Collections.singletonList(new CompositeAccessTokenProvider()));
    }

    @Value("${ssoServiceUrl:placeholder}")
    private String ssoServiceUrl;

    @Autowired
    @Qualifier("passwordGrantRestTemplate")
    private OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @Autowired
    private ResourceOwnerPasswordResourceDetails passwordGrantResourceDetails;

    @RequestMapping("/password")
    public String showPasswordPage() {
        if (this.ssoServiceUrl.equals("placeholder")) {
            return "configure_warning";
        }
        return "password_form";
    }

    @RequestMapping(value = "/password",method = RequestMethod.POST)
    public String doPasswordLogin(@RequestParam String username, @RequestParam String password, Model model) throws Exception {
        passwordGrantResourceDetails.setUsername(username);
        passwordGrantResourceDetails.setPassword(password);
        String response = oAuth2RestTemplate.getForObject("{uaa}/userinfo", String.class,
            ssoServiceUrl);
        model.addAttribute("response", toPrettyJsonString(response));
        Map<String, ?> token = getToken(oAuth2RestTemplate.getOAuth2ClientContext());
        model.addAttribute("token", toPrettyJsonString(token));
        model.addAttribute("id_token", toPrettyJsonString(getIdToken(oAuth2RestTemplate.getOAuth2ClientContext())));
        return "password_result";
    }

    private Map<String, ?> getIdToken(OAuth2ClientContext clientContext) throws IOException {
        if (clientContext.getAccessToken() != null) {
            String tokenBase64 = clientContext.getAccessToken().getAdditionalInformation().get("id_token").toString().split("\\.")[1];
            return objectMapper.readValue(Base64.decodeBase64(tokenBase64), new TypeReference<Map<String, ?>>() {
            });
        }
        return null;
    }

    private Map<String, ?> getToken(OAuth2ClientContext clientContext) throws Exception {
        if (clientContext.getAccessToken() != null) {
            String tokenBase64 = clientContext.getAccessToken().getValue().split("\\.")[1];
            return objectMapper.readValue(Base64.decodeBase64(tokenBase64), new TypeReference<Map<String, ?>>() {
            });
        }
        return null;
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.client")
    ResourceOwnerPasswordResourceDetails passwordGrantResourceDetails() {
        return new ResourceOwnerPasswordResourceDetails();
    }

    @Bean
    public OAuth2RestTemplate passwordGrantRestTemplate() {
        return new OAuth2RestTemplate(passwordGrantResourceDetails());
    }


    @RequestMapping(value="/logout", method = GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.oAuth2RestTemplate.getOAuth2ClientContext().setAccessToken(null);
        return "redirect:/password";
    }
}
