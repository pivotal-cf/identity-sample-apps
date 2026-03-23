package io.pivotal.identityService.samples.authcodeclientcredentials.configuration;

import io.pivotal.identityService.samples.authcodeclientcredentials.security.UaaLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private UaaLogoutSuccessHandler uaaLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/", "/client/todos", "/client/todos/*").permitAll()
                    .anyRequest().authenticated()
                .and()
                    .oauth2Login()
                        .loginPage("/oauth2/authorization/ssoauthorizationcode")
                        .failureUrl("/login?error")
                        .permitAll()
                .and()
                    .logout()
                        .logoutSuccessHandler(uaaLogoutSuccessHandler);
        return http.build();
    }
}
