package org.fluentlenium.examples.quickstart;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;


@Wait
public class AuthorizationCodeTest extends FluentTest {
    @Test
    public void securesHelloWorldWithAccessToken() {
        goTo("http://localhost:8888/secured/token");

        $("input[name=username]").fill().with("sample-user");
        $("input[name=password]").fill().with("sample-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        assertThat(el("body").text()).contains("user@example.com");
        assertThat(el("body").text()).contains("sample-user");
        assertThat(el("body").text()).contains("sample-client");
        assertThat(el("body").text()).contains("sample.scope");
    }
}
