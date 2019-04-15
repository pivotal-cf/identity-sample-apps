package io.pivotal.identityService.samples.authcode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${applicationUrl:placeholder}")
    String appUrl;

    @Value("${ssoServiceUrl:http://localhost:8080}")
    String ssoServiceUrl;

    @Value("${spring.security.oauth2.client.registration.sso.client-id:placeholder}")
    String clientId;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/").permitAll()
                    .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .and()
                .logout()
                    .logoutSuccessUrl(getLogoutUrl());
    }

    private String getLogoutUrl() {
        return String.format("%s/logout.do?client_id=%s&redirect=%s", ssoServiceUrl, clientId, appUrl);
    }
}
