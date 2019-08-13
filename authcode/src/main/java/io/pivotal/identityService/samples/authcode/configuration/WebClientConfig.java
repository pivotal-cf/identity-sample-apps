package io.pivotal.identityService.samples.authcode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${RESOURCE_URL}")
    private String resourceServerUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(resourceServerUrl)
                .build();
    }
}
