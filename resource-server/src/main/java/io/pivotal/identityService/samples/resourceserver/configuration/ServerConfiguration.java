package io.pivotal.identityService.samples.resourceserver.configuration;

import io.pivotal.identityService.samples.resourceserver.security.AudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;

@Configuration
public class ServerConfiguration {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwksUri;

    @Value("${sso.issuer}")
    private String issuer;

    @Value("${sso.audience:}")
    private String audience;

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoderJwkSupport jwtDecoder = new NimbusJwtDecoderJwkSupport(jwksUri);

        // Validate `iss` and `aud` claims from application.yml
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new AudienceValidator(audience)
        ));

        return jwtDecoder;
    }
}
