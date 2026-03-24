package io.pivotal.identityService.samples.authcodeclientcredentials.configuration;

import io.pivotal.identityService.samples.authcodeclientcredentials.security.UaaLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private UaaLogoutSuccessHandler uaaLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/", "/client/todos", "/client/todos/*").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(grantedAuthoritiesMapper()))
                    .loginPage("/oauth2/authorization/ssoauthorizationcode")
                    .failureUrl("/login?error")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutSuccessHandler(uaaLogoutSuccessHandler)
                );
        return http.build();
    }

    private GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach((authority) -> {
                GrantedAuthority mappedAuthority;
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority userAuthority = (OidcUserAuthority) authority;
                    mappedAuthority = new OidcUserAuthority("ROLE_USER", userAuthority.getIdToken(), userAuthority.getUserInfo());
                } else if (authority instanceof OAuth2UserAuthority) {
                    OAuth2UserAuthority userAuthority = (OAuth2UserAuthority) authority;
                    mappedAuthority = new OAuth2UserAuthority("ROLE_USER", userAuthority.getAttributes());
                } else {
                    mappedAuthority = authority;
                }
                mappedAuthorities.add(mappedAuthority);
            });
            return mappedAuthorities;
        };
    }
}
