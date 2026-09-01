package io.pivotal.identityService.samples.resourceserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator();

    private Jwt jwtWithAudience(List<String> audience) {
        return new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("aud", audience, "sub", "test-subject"));
    }

    @Test
    void succeedsWhenAudienceContainsTodo() {
        var result = validator.validate(jwtWithAudience(List.of("uaa", "todo", "sample-client")));
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void failsWhenAudienceDoesNotContainTodo() {
        var result = validator.validate(jwtWithAudience(List.of("uaa", "sample-client")));
        assertThat(result.hasErrors()).isTrue();
        var error = result.getErrors().iterator().next();
        assertThat(error.getErrorCode()).isEqualTo("invalid_token");
        assertThat(error.getDescription()).isEqualTo("The required audience is missing");
    }
}
