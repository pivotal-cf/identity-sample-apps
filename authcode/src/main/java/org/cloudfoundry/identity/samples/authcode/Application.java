package org.cloudfoundry.identity.samples.authcode;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/authorization_code")
    public String authCode(Model model) throws Exception {
        if (oauth2RestTemplate == null) {
            return "configure_warning";
        }
        Map<?,?> userInfoResponse = oauth2RestTemplate.getForObject("{ssoServiceUrl}/userinfo", Map.class,
                ssoServiceUrl);
        model.addAttribute("ssoServiceUrl",ssoServiceUrl);
        model.addAttribute("response",toPrettyJsonString(userInfoResponse));
        Map<String, ?> token = getToken(oauth2RestTemplate.getOAuth2ClientContext());
        model.addAttribute("token",toPrettyJsonString(token));
        return "authorization_code";
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
}
