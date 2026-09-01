package io.pivotal.identityService.samples.resourceserver.configuration;


import io.pivotal.identityService.samples.resourceserver.security.LegacyBearerTokenAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint entryPoint = new LegacyBearerTokenAuthenticationEntryPoint();
        http
                .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .authenticationEntryPoint(entryPoint)
                    .jwt(jwt -> {})
                )
                // Spring Security 7 unconditionally registers OAuth2ProtectedResourceMetadataFilter,
                // which serves RFC 9728 metadata at this path. On Spring Security 6.5 the path was
                // simply unmatched and fell through to anyRequest().authenticated(), yielding 401.
                // These sample apps are consumed as a fixed contract by other projects' integration
                // tests, so the endpoint surface is kept as it was. Remove this filter (and the
                // custom entry point above) to adopt the RFC 9728 behavior.
                //
                // Anchored on SecurityContextHolderFilter because addFilterBefore requires a filter
                // Spring Security knows the order of, and OAuth2ProtectedResourceMetadataFilter is
                // not in that registry. Anything earlier than the metadata filter works.
                .addFilterBefore(new SuppressProtectedResourceMetadataFilter(entryPoint),
                        SecurityContextHolderFilter.class);
        return http.build();
    }

    private static final class SuppressProtectedResourceMetadataFilter extends OncePerRequestFilter {

        private static final String PATH = "/.well-known/oauth-protected-resource";

        private final AuthenticationEntryPoint entryPoint;

        private SuppressProtectedResourceMetadataFilter(AuthenticationEntryPoint entryPoint) {
            this.entryPoint = entryPoint;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            if (path.equals(PATH) || path.startsWith(PATH + "/")) {
                this.entryPoint.commence(request, response,
                        new AuthenticationCredentialsNotFoundException("Full authentication is required"));
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
