package io.pivotal.cf.identity.samples.resourceServer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@EnableWebSecurity
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2ResourceServerProperties resourceServerProperties;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer()
                    .jwt()
                        .jwkSetUri(this.resourceServerProperties.getJwt().getJwkSetUri());
    }
}
