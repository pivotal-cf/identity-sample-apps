/*******************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2016] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/

import org.cloudfoundry.identity.app.web.SSLValidationDisabler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import java.util.logging.Logger;

/**
 *
 * @author Dave Syer
 *
 */
public class OAuth2AccessTokenSource implements InitializingBean, PreAuthenticatedPrincipalSource<String> {

  private OAuth2RestOperations restTemplate;
  /**
   * A rest template to be used to contact the remote user info endpoint.
   * Normally an instance of {@link OAuth2RestTemplate}.
   *
   * @param restTemplate a rest template
   */
  public void setRestTemplate(OAuth2RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }
  @Override
  public void afterPropertiesSet() {
    Assert.state(restTemplate != null, "RestTemplate URL must be provided");
  }

  @Override
  public String getPrincipal() {
    SSLValidationDisabler.disableSSLValidation();
    return
      restTemplate.getAccessToken().getValue();
  }
}
