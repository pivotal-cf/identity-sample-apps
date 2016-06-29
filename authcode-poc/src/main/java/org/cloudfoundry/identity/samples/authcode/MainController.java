package org.cloudfoundry.identity.samples.authcode;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@Component
public class MainController {
  public MainController() {
  }

  @Value("${ssoServiceUrl:placeholder}")
  private String ssoServiceUrl;

  @Value("${security.oauth2.client.clientId:placeholder}")
  private String clientId;

  @Autowired(required = false)
  private OAuth2RestTemplate oauth2RestTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @RequestMapping("/")
  public String index() {
    return "index";
  }

  @RequestMapping("/authorization_code")
  public String authCode(Model model, HttpServletRequest request) throws Exception {
    if (ssoServiceUrl.equals("placeholder")) {
      model.addAttribute("header", "Warning: You need to bind to the SSO service.");
      model.addAttribute("warning", "Please bind your app to restore regular functionality");
      return "configure_warning";
    }
    Map<?,?> userInfoResponse = oauth2RestTemplate.getForObject("{ssoServiceUrl}/userinfo", Map.class, ssoServiceUrl);
    model.addAttribute("ssoServiceUrl",ssoServiceUrl);
    model.addAttribute("response", toPrettyJsonString(userInfoResponse));

    OAuth2AccessToken accessToken = oauth2RestTemplate.getOAuth2ClientContext().getAccessToken();
    if (accessToken != null) {
      model.addAttribute("access_token", toPrettyJsonString(parseToken(accessToken.getValue())));
      model.addAttribute("id_token", toPrettyJsonString(parseToken((String) accessToken.getAdditionalInformation().get("id_token"))));
    }
    return "authorization_code";
  }

  @RequestMapping(value="/logout", method = GET)
  public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null){
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    URL url = new URL(request.getRequestURL().toString());
    String urlStr = url.getProtocol() + "://" + url.getAuthority();
    return "redirect:" + ssoServiceUrl + "/logout.do?redirect=" + urlStr + "&clientId=" + clientId;
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
