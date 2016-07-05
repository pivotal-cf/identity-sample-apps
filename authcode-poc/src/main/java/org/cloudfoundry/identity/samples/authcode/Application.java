package org.cloudfoundry.identity.samples.authcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebMvc
@EnableOAuth2Client
@PropertySource("classpath:application.properties")
@Import(SampleWebSecurityConfiguration.class)
public class Application extends WebMvcConfigurerAdapter {

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/templates/**").addResourceLocations("/templates/");
  }

  @Bean
  public InternalResourceViewResolver internalResourceViewResolver() {
    InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
    internalResourceViewResolver.setPrefix("/templates/");
    internalResourceViewResolver.setSuffix(".html");
    return internalResourceViewResolver;
  }
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public MainController mainController() {
    return new MainController();
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Autowired
  private OAuth2RestTemplate oauth2RestTemplate;

  @PostConstruct
  public void init() {
      oauth2RestTemplate.setAccessTokenProvider(accessTokenProviderChain());
      SSLValidationDisabler.disableSSLValidation();
  }

  @Bean
  public AccessTokenProvider accessTokenProviderChain() {
    return new AuthorizationCodeAccessTokenProvider();
  }

  @Bean
  public OAuth2RestTemplate oauth2RestTemplate() {
    AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
    details.setClientId("account-test");
    details.setClientSecret("account_secret");
    details.setUseCurrentUri(true);
    details.setAccessTokenUri("https://login.identity.cf-app.com/oauth/token");
    details.setPreEstablishedRedirectUri("http://localhost:8080/authorizationcode");
    details.setUserAuthorizationUri("https://login.identity.cf-app.com/oauth/authorize");
    return new OAuth2RestTemplate(details);
  }
}
