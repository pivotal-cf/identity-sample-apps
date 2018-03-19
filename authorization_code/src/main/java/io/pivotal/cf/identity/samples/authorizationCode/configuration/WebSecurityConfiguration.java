package io.pivotal.cf.identity.samples.authorizationCode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.REFERER;

@Configuration
@EnableOAuth2Sso
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${ssoServiceUrl}")
    private String ssoServiceUrl;

    @Value("${security.oauth2.client.clientId}")
    private String clientId;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/secured/**").authenticated()
            .and()
                .logout().logoutSuccessHandler(logoutSuccessHandler())
                .permitAll();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String appLogoutSuccessUrl = request.getHeader(REFERER);
            String authServerLogoutUrl = ssoServiceUrl + "/logout.do?redirect=" + appLogoutSuccessUrl + "&client_id=" + clientId;
            response.setHeader("Location", authServerLogoutUrl);
            response.setStatus(HttpServletResponse.SC_FOUND);
        };
    }
}
