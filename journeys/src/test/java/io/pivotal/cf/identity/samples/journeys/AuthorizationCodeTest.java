package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;


@Wait
public class AuthorizationCodeTest extends FluentTest {
    @Test
    public void displaysAccessTokenToAllUsers() {
        goTo("http://localhost:8888/secured/access_token");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("user@example.com");
        assertThat(el("body").text()).contains("basic-user");
        assertThat(el("body").text()).contains("sample-client");
        assertThat(el("body").text()).contains("openid");

        $("#logout").click();
        assertThat(url()).contains("/uaa/login");

        goTo("http://localhost:8888/secured/token");
        assertThat(url()).contains("/uaa/login");
    }

    @Test
    public void displaysUserinfoToAllUsers() {
        goTo("http://localhost:8888/secured/userinfo");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("FirstName LastName");

        $("#logout").click();
    }

    @Test
    public void basicUser() {
        goTo("http://localhost:8888/secured/abc");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Invalid token");

        goTo("http://localhost:8888/secured/access_token");
        $("#logout").click();
    }

    @Test
    public void abcUser() {
        goTo("http://localhost:8888/secured/abc");

        $("input[name=username]").fill().with("abc-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Verified that token contains acme.abc");

        goTo("http://localhost:8888/secured/xyz");
        assertThat(el("body").text()).contains("Insufficient scope for this resource");


        goTo("http://localhost:8888/secured/access_token");
        $("#logout").click();
    }

    @Test
    public void xyzUser() {
        goTo("http://localhost:8888/secured/xyz");

        $("input[name=username]").fill().with("xyz-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Verified that token contains acme.xyz");

        goTo("http://localhost:8888/secured/abc");
        assertThat(el("body").text()).contains("Insufficient scope for this resource");

        goTo("http://localhost:8888/secured/access_token");
        $("#logout").click();
    }
}
