package io.pivotal.identityService.samples.authcode.configuration;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${RESOURCE_URL}")
    private String resourceServerUrl;

    @Value("${TRUST_CERTS:}")
    private String trustCerts;

    private final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient webClient(ClientRegistrationRepository clientRegistrationRepository,
                               OAuth2AuthorizedClientRepository authorizedClientRepository) throws Exception {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository,
                        authorizedClientRepository);
        oauth2.setDefaultOAuth2AuthorizedClient(true);
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(resourceServerUrl)
                .apply(oauth2.oauth2Configuration());
        if (shouldSkipSslValidation()) {
            builder.clientConnector(insecureClientConnector());
        }
        return builder.build();
    }

    private boolean shouldSkipSslValidation() {
        return !StringUtils.isEmpty(trustCerts);
    }

    private ReactorClientHttpConnector insecureClientConnector() throws SSLException, NoSuchAlgorithmException {
        // #yolo trust everything
        // This is an awful quickfix
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return new ReactorClientHttpConnector(httpClient);
    }
}
