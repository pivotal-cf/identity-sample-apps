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

        String userInfo = el(".user_info").text();
        assertThat(userInfo).contains("FirstName LastName");

        String accessToken = el(".access_token").text();
        assertThat(accessToken).contains("basic-user@example.com");
        assertThat(accessToken).contains("basic-user");
        assertThat(accessToken).contains("sample-client-authcode");
        assertThat(accessToken).contains("openid");
        assertThat(accessToken).contains("authorization_code");

        String idToken = el(".id_token").text();
        assertThat(idToken).contains("openid");

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

        assertThat(el("body").text()).contains("401");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");
        $("#logout").click();
    }

    @Test
    public void readOnlyUser() {
        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");

        $("input[name=username]").fill().with("read-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List")).click();

        assertThat(el("body").text()).contains("seed-task-1");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("403");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/todos");

        el("input[value=Delete]").click();
        assertThat(el("body").text()).contains("403");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");
        $("#logout").click();
    }

    @Test
    public void readWriteUser() {
        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");

        $("input[name=username]").fill().with("read-write-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List")).click();

        assertThat(el("body").text()).contains("seed-task-1");
        assertThat(el("body").text()).doesNotContain("all the things");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("all the things");

        $("tbody tr:last-child input[value=Delete]").click();

        assertThat(el("body").text()).doesNotContain("all the things");

        goTo(AUTHCODE_CLIENT_BASE_URL + "/info");
        $("#logout").click();
    }
}
