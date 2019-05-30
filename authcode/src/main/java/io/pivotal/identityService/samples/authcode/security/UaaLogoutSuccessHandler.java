package io.pivotal.identityService.samples.authcode.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class UaaLogoutSuccessHandler implements LogoutSuccessHandler {
    @Value("${ssoServiceUrl:placeholder}")
    String ssoServiceUrl;

    @Value("${spring.security.oauth2.client.registration.sso.client-id:placeholder}")
    String clientId;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UriComponents url = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .replacePath("")
                .build();
        UriComponents redirectUrl = UriComponentsBuilder.fromHttpUrl(ssoServiceUrl)
                .path("/logout.do")
                .queryParam("client_id", clientId)
                .queryParam("redirect", url.toString())
                .build();
        response.sendRedirect(redirectUrl.toString());
    }
}
