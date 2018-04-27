package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Ignore;
import org.junit.Test;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;


@Wait
public class AuthorizationCodeTest extends FluentTest {
    @Test
    public void displaysAccessTokenToAllUsers() {
        goTo("http://localhost:8888/secured/access_token");

        $("input[name=username]").fill().with("sample-user");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("user@example.com");
        assertThat(el("body").text()).contains("sample-user");
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

        $("input[name=username]").fill().with("sample-user");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("FirstName LastName");

        $("#logout").click();
    }

    @Test
    public void doesNotDisplayResourcesToUnauthorizedUsers() {
        goTo("http://localhost:8888/secured/todos/read");

        $("input[name=username]").fill().with("sample-user");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Invalid token");
    }

    @Test
    public void todoReaders() {
        goTo("http://localhost:8888/secured/todos/read");

        $("input[name=username]").fill().with("sample-todo-reader");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Verified that token contains todo.read");

        $("#logout").click();
    }

    @Test
    public void todoWriters() {
        goTo("http://localhost:8888/secured/todos/write");

        $("input[name=username]").fill().with("sample-todo-writer");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("Verified that token contains todo.read and todo.write");

        $("#logout").click();
    }
}
