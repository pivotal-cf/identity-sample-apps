package org.cloudfoundry.identity.samples.oauth2.composite;

import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;

public class CompositeAccessTokenProvider extends ResourceOwnerPasswordAccessTokenProvider {

    @Override
    public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details, AccessTokenRequest request)
        throws UserRedirectRequiredException, AccessDeniedException, OAuth2AccessDeniedException {

        ResourceOwnerPasswordResourceDetails resource = (ResourceOwnerPasswordResourceDetails) details;
        return retrieveToken(request, resource, getParametersForTokenRequest(resource, request), new HttpHeaders());

    }

    private MultiValueMap<String, String> getParametersForTokenRequest(ResourceOwnerPasswordResourceDetails resource, AccessTokenRequest request) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.set("grant_type", "password");
        form.set("response_type", "id_token");

        form.set("username", resource.getUsername());
        form.set("password", resource.getPassword());
        form.putAll(request);

        if (resource.isScoped()) {

            StringBuilder builder = new StringBuilder();
            List<String> scope = resource.getScope();

            if (scope != null) {
                Iterator<String> scopeIt = scope.iterator();
                while (scopeIt.hasNext()) {
                    builder.append(scopeIt.next());
                    if (scopeIt.hasNext()) {
                        builder.append(' ');
                    }
                }
            }

            form.set("scope", builder.toString());
        }

        return form;

    }
}
