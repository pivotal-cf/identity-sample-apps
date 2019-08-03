package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.containingText;


@Wait
public class ClientCredentialsTest extends FluentTest {
    private static final String CLIENT_CREDENTIALS_BASE_URL = "http://localhost:8887";

    @Test
    public void displaysToken() {
        goTo(CLIENT_CREDENTIALS_BASE_URL + "/info");

        String accessToken = el(".access_token").text();
        assertThat(accessToken).contains("sample-client-client-credentials");
        assertThat(accessToken).contains("\"todo.read\", \"todo.write\", \"uaa.resource\"");
        assertThat(accessToken).contains("\"uaa.resource\", \"todo.read\", \"todo.write\"");
        assertThat(accessToken).contains("client_credentials");
    }

    @Test
    public void readWriteClient() {
        goTo(CLIENT_CREDENTIALS_BASE_URL + "/info");

        $("a", containingText("TODO List")).click();

        assertThat(el("body").text()).contains("seed-task-1");
        assertThat(el("body").text()).doesNotContain("all the things");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("all the things");

        $("tbody tr:last-child input[value=Delete]").click();

        assertThat(el("body").text()).doesNotContain("all the things");
    }
}
