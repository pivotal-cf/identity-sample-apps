package io.pivotal.identityService.samples.authcode.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${RESOURCE_URL}")
    private String resourceServerUrl;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public RestClient restClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor oauth2 =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        // The equivalent of the WebClient filter's setDefaultOAuth2AuthorizedClient(true):
        // send the token belonging to the registration this user logged in with. The
        // interceptor's default resolver reads a per-request attribute that nothing here
        // sets, and on a null id it returns without adding an Authorization header at all
        // -- so the resource server just answers 401 with no hint as to why.
        oauth2.setClientRegistrationIdResolver(request -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return (authentication instanceof OAuth2AuthenticationToken oauth2Token)
                    ? oauth2Token.getAuthorizedClientRegistrationId()
                    : null;
        });
        return RestClient.builder()
                .baseUrl(resourceServerUrl)
                .requestInterceptor(oauth2)
                .build();
    }
}
