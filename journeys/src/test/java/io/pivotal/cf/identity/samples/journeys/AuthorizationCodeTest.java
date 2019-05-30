package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.containingText;


@Wait
public class AuthorizationCodeTest extends FluentTest {
    public static final String AUTHCODE_CLIENT_BASE_URL = "http://localhost:8888";

    @Test
    public void displaysTokensToAllUsers() {
        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        String accessToken = el(".access_token").text();
        assertThat(accessToken).contains("basic-user@example.com");
        assertThat(accessToken).contains("basic-user");
        assertThat(accessToken).contains("sample-client");
        assertThat(accessToken).contains("openid");

        String userInfo = el(".user_info").text();
        assertThat(userInfo).contains("FirstName LastName");

        $("#logout").click();
        assertThat(url()).isEqualTo(AUTHCODE_CLIENT_BASE_URL + "/");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");
        assertThat(url()).contains("/uaa/login");
    }

    @Test
    public void basicUserIsDeniedAccessToResource() {
        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List")).click();

        assertThat(el("body").text()).contains("401 UNAUTHORIZED");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");
        $("#logout").click();
    }

//    @Test
//    public void abcUser() {
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/abc");
//
//        $("input[name=username]").fill().with("abc-user");
//        $("input[name=password]").fill().with("example-password");
//        $("input[type=submit]").submit();
//
//        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
//            $("#authorize").click();
//        }
//
//        assertThat(el("body").text()).contains("Verified that token contains acme.abc");
//
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/xyz");
//        assertThat(el("body").text()).contains("Insufficient scope for this resource");
//
//
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/access_token");
//        $("#logout").click();
//    }
//
//    @Test
//    public void xyzUser() {
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/xyz");
//
//        $("input[name=username]").fill().with("xyz-user");
//        $("input[name=password]").fill().with("example-password");
//        $("input[type=submit]").submit();
//
//        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
//            $("#authorize").click();
//        }
//
//        assertThat(el("body").text()).contains("Verified that token contains acme.xyz");
//
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/abc");
//        assertThat(el("body").text()).contains("Insufficient scope for this resource");
//
//        goTo(AUTHCODE_CLIENT_BASE_URL + "/secured/access_token");
//        $("#logout").click();
//    }
}
