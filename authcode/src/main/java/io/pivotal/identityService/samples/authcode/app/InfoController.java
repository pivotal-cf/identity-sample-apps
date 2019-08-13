package io.pivotal.identityService.samples.authcode.app;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
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
        byte[] token = Base64.decodeBase64(authorizationHeader.replace("Bearer ", ""));
        try {
            DecodedJWT jwt = JWT.decode(new String(token));
            model.addAttribute("access_token", toPrettyJsonString(jwt.getClaims()));
            model.addAttribute("authorization_header", authorizationHeader);
        } catch (JWTDecodeException exception){
            //Invalid token
        }

        return "info";
    }

    private String toPrettyJsonString(Object object) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
