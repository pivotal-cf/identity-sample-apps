package io.pivotal.identityService.samples.authcode.app;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoController {

    private ObjectMapper objectMapper;

    public InfoController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/info")
    public String authorizationCode(HttpServletRequest request, Model model) throws Exception {
        prepareHeaderInfo(request, model, "Authorization", "authorization_header", "authorization_header_token");
        prepareHeaderInfo(request, model, "x-id-token", "x_id_token_header", "x_id_token_header_token");
        return "info";
    }

    private void prepareHeaderInfo(HttpServletRequest request, Model model, String headerName, String headerModelKey,
                                   String tokenHeaderKey) throws IOException {
        String authorizationHeader = request.getHeader(headerName);
        if (authorizationHeader == null) {
            authorizationHeader = "no Authorization header included";
        }
        model.addAttribute(headerModelKey, authorizationHeader);

        String authorizationHeaderToken = authorizationHeader.replace("Bearer ", "");
        try {
            model.addAttribute(tokenHeaderKey, tokenClaimsAsPrettyPrintedJson(authorizationHeaderToken));
        } catch (JWTDecodeException exception) {
            model.addAttribute(tokenHeaderKey, "the token was invalid");
        }
    }

    private String tokenClaimsAsPrettyPrintedJson(String token) throws IOException {
        String jsonString = new String(Base64.getDecoder().decode(JWT.decode(token).getPayload()));
        HashMap<String, Object> hashMap = objectMapper.readValue(jsonString, new TypeReference<HashMap<String, Object>>() {
        });
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hashMap);
    }

}
