import io.pivotal.spring.cloud.SsoServiceInfo;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SsoServiceInfoCreatorInitializer {

    private CloudFactory cloudFactory;
    private Environment environment;

    private static final String PROPERTY_SOURCE_NAME = "vcapPivotalSso";
    private static final String SPRING_OAUTH2_CLIENT_ID = "clientId";
    private static final String SPRING_OAUTH2_CLIENT_SECRET = "clientSecret";
    private static final String SPRING_OAUTH2_AUTHORIZE_URI = "userAuthorizationUri";
    private static final String SPRING_OAUTH2_KEY_URI = "keyUri";
    private static final String SPRING_OAUTH2_ACCESS_TOKEN_URI = "accessTokenUri";
    private static final String SSO_SERVICE_URL = "ssoServiceUrl";
    private static final String SPRING_OAUTH2_USER_INFO_URI = "userInfoUri";
    private static final String SPRING_OAUTH2_TOKEN_INFO_URI = "tokenInfoUri";

    public SsoServiceInfoCreatorInitializer(CloudFactory cloudFactory, Environment environment) {
        this.cloudFactory = cloudFactory;
        this.environment = environment;
        List<ServiceInfo> serviceInfos = cloudFactory.getCloud().getServiceInfos();
        for(ServiceInfo s : serviceInfos) {
            if (s instanceof SsoServiceInfo) {
                Map<String, Object> map = new HashMap<String, Object>();
                SsoServiceInfo ssoServiceInfo = (SsoServiceInfo) s;
                map.put(SPRING_OAUTH2_CLIENT_ID, ssoServiceInfo.getClientId());
                map.put(SPRING_OAUTH2_CLIENT_SECRET, ssoServiceInfo.getClientSecret());
                map.put(SPRING_OAUTH2_ACCESS_TOKEN_URI, ssoServiceInfo.getAuthDomain() + "/oauth/token");
                map.put(SPRING_OAUTH2_AUTHORIZE_URI, ssoServiceInfo.getAuthDomain() + "/oauth/authorize");
                map.put(SPRING_OAUTH2_KEY_URI, ssoServiceInfo.getAuthDomain() + "/token_key");
                map.put(SSO_SERVICE_URL, ssoServiceInfo.getAuthDomain());
                map.put(SPRING_OAUTH2_USER_INFO_URI, ssoServiceInfo.getAuthDomain() + "/userinfo");
                map.put(SPRING_OAUTH2_TOKEN_INFO_URI, ssoServiceInfo.getAuthDomain() + "/check_token");
                MapPropertySource mapPropertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, map);

                ((ConfigurableEnvironment) environment).getPropertySources().addFirst(mapPropertySource);
            }
        }
    }
}
