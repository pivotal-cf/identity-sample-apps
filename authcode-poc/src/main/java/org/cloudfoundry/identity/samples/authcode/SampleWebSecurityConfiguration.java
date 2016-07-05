package org.cloudfoundry.identity.samples.authcode;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

@Configuration
@EnableWebSecurity
public class SampleWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    OAuth2AuthenticationProcessingFilter filter = new OAuth2AuthenticationProcessingFilter();
    http.addFilterBefore(filter, WebAsyncManagerIntegrationFilter.class);

    http.authorizeRequests()
     .antMatchers("/").permitAll();
  }
}
