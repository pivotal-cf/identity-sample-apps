package io.pivotal.identityService.samples.resourceserver.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private List<String> audiences;

    public AudienceValidator(String audience) {
        this.audiences = Arrays.stream(audience.split(","))
                .filter(a -> a.length() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (audiences.size() < 1) {
            return OAuth2TokenValidatorResult.success();
        }

        for (String audience : audiences) {
            if (jwt.getAudience().contains(audience.trim())) {
                return OAuth2TokenValidatorResult.success();
            }
        }

        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "One or more of the required audience(s) are missing", null)
        );
    }
}
