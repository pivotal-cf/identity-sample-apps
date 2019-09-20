package io.pivotal.identityService.samples.authcode.app;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class InfoController {
    private ObjectMapper objectMapper;

    public InfoController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/info")
    public String authorizationCode(HttpServletRequest request,
                                    Model model) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null) {
            authorizationHeader = "no Authorization header included";
        }
        model.addAttribute("authorization_header", authorizationHeader);

        String idToken = authorizationHeader.replace("Bearer ", "");
        try {
            model.addAttribute("id_token", tokenClaimsAsPrettyPrintedJson(idToken));
        } catch (JWTDecodeException exception) {
            model.addAttribute("id_token", "the token was invalid");
        }

        return "info";
    }

    private String tokenClaimsAsPrettyPrintedJson(String token) throws IOException {
        String jsonString = new String(Base64.getDecoder().decode(JWT.decode(token).getPayload()));
        HashMap<String, Object> hashMap = objectMapper.readValue(jsonString, new TypeReference<HashMap<String, Object>>() {
        });
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hashMap);
    }

}
