package io.pivotal.cf.identity.samples.authorizationCode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletResponse;

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

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails oa2prd, OAuth2ClientContext oa2cc) {
        return new OAuth2RestTemplate(oa2prd, oa2cc);
    }
}
