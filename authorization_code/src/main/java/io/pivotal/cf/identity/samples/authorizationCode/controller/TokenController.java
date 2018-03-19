package io.pivotal.cf.identity.samples.authorizationCode.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class TokenController {

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/secured/token")
    public String show() {
        OAuth2Authentication auth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)auth.getDetails();
        String tokenValue = details.getTokenValue();
        
        return "<html>\n" +
                "<body>\n" +
                "\n" +
                "<form action=\"/logout\" method=\"POST\">\n" +
                "    <button id=\"logout\" type=\"submit\">Logout</button>\n" +
                "</form>\n" +
                "\n" +
                "<pre>" + formatJwtToken(tokenValue) + "</pre>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    private String formatJwtToken(String token) {
        try {
            return toPrettyJsonString(parseToken(token));
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, ?> parseToken(String base64Token) throws IOException {
        String token = base64Token.split("\\.")[1];
        return this.objectMapper.readValue(Base64.decodeBase64(token),
                new TypeReference<Map<String, ?>>() {});
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
