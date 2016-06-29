package org.cloudfoundry.identity.samples.authcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(value = Component.class))
@EnableWebMvc
public class Application {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
