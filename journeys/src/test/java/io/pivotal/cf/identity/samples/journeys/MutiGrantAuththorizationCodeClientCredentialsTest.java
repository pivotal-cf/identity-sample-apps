package io.pivotal.cf.identity.samples.journeys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluentlenium.adapter.junit.FluentTest;
import org.fluentlenium.core.hook.wait.Wait;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fluentlenium.assertj.FluentLeniumAssertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.containingText;


@Wait
public class MutiGrantAuththorizationCodeClientCredentialsTest extends FluentTest {
    private static final String MULTI_GRANT_BASE_URL = "http://localhost:8890";
    private static final TypeReference<Map<String, Object>> TYPE_REF = new TypeReference<Map<String, Object>>() {};
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void displaysClientCredentialsToken() {
        goTo(MULTI_GRANT_BASE_URL);

        String accessToken = el(".access_token").text();
        assertThat(accessToken).contains("sample-client-authcode-client-credentials");
        assertThat(accessToken).contains("\"todo.read\", \"todo.write\", \"uaa.resource\"");
        assertThat(accessToken).contains("\"uaa.resource\", \"todo.read\", \"todo.write\"");
        assertThat(accessToken).contains("client_credentials");
    }

    @Test
    public void readWriteClientCredentialsClient() {
        goTo(MULTI_GRANT_BASE_URL);

        $("a", containingText("TODO List using the client credentials token")).click();

        assertThat(el("body").text()).contains("seed-task-1");
        assertThat(el("body").text()).doesNotContain("all the things");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("all the things");

        $("tbody tr:last-child input[value=Delete]").click();

        assertThat(el("body").text()).doesNotContain("all the things");
    }

    @Test
    public void displaysAuthcodeTokenAndClientCredentialsTokenToAllLoggedInUsers() throws IOException {
        goTo(MULTI_GRANT_BASE_URL + "/info");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        HashMap<String, Object> userInfo = objectMapper.readValue(el(".authcode .user_info").text(), TYPE_REF);
        assertThat(userInfo.get("name")).isEqualTo("FirstName LastName");

        HashMap<String, Object> authcodeAccessToken = objectMapper.readValue(el(".authcode .access_token").text(), TYPE_REF);
        assertThat(authcodeAccessToken.get("email")).isEqualTo("basic-user@example.com");
        assertThat(authcodeAccessToken.get("user_name")).isEqualTo("basic-user");
        assertThat(authcodeAccessToken.get("cid")).isEqualTo("sample-client-authcode-client-credentials");
        assertThat((List<String>)authcodeAccessToken.get("scope")).contains("openid", "profile", "roles", "user_attributes", "email");
        assertThat(authcodeAccessToken.get("grant_type")).isEqualTo("authorization_code");
        assertThat((List<String>)authcodeAccessToken.get("aud")).contains("sample-client-authcode-client-credentials", "openid");

        HashMap<String, Object> idToken = objectMapper.readValue(el(".authcode .id_token").text(), TYPE_REF);
        assertThat((List<String>)idToken.get("scope")).contains("openid");

        HashMap<String, Object> clientCredentialsAccessToken = objectMapper.readValue(el(".clientcreds .access_token").text(), TYPE_REF);
        assertThat(clientCredentialsAccessToken.get("client_id")).isEqualTo("sample-client-authcode-client-credentials");
        assertThat(clientCredentialsAccessToken.get("cid")).isEqualTo("sample-client-authcode-client-credentials");
        assertThat((List<String>)clientCredentialsAccessToken.get("scope")).contains("todo.read", "todo.write", "uaa.resource");
        assertThat((List<String>)clientCredentialsAccessToken.get("authorities")).contains("uaa.resource", "todo.read", "todo.write");
        assertThat(clientCredentialsAccessToken.get("grant_type")).isEqualTo("client_credentials");
        assertThat((List<String>)clientCredentialsAccessToken.get("aud")).contains("todo", "sample-client-authcode-client-credentials", "uaa");

        $("#logout").click();
        assertThat(url()).isEqualTo(MULTI_GRANT_BASE_URL + "/");

        goTo(MULTI_GRANT_BASE_URL + "/info");
        assertThat(url()).contains("/uaa/login");
    }

    @Test
    public void basicUserAuthcodeIsDeniedAccessToResource() {
        goTo(MULTI_GRANT_BASE_URL + "/info");

        $("input[name=username]").fill().with("basic-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List using the authcode user token")).click();

        assertThat(el("body").text()).contains("401 UNAUTHORIZED");

        goTo(MULTI_GRANT_BASE_URL + "/info");
        $("#logout").click();
    }

    @Test
    public void readOnlyAuthcodeUser() {
        goTo(MULTI_GRANT_BASE_URL + "/info");

        $("input[name=username]").fill().with("read-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List using the authcode user token")).click();

        assertThat(el("body").text()).contains("seed-task-1");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("403");

        goTo(MULTI_GRANT_BASE_URL + "/user/todos");

        el("input[value=Delete]").click();
        assertThat(el("body").text()).contains("403");

        goTo(MULTI_GRANT_BASE_URL + "/info");
        $("#logout").click();
    }

    @Test
    public void readWriteAuthcodeUser() {
        goTo(MULTI_GRANT_BASE_URL + "/info");

        $("input[name=username]").fill().with("read-write-user");
        $("input[name=password]").fill().with("example-password");
        $("input[type=submit]").submit();

        if ($("h1").present() && el("h1").text().equals("Application Authorization")) {
            $("#authorize").click();
        }

        $("a", containingText("TODO List using the authcode user token")).click();

        assertThat(el("body").text()).contains("seed-task-1");
        assertThat(el("body").text()).doesNotContain("all the things");

        $("input[name=task]").fill().with("all the things");
        $("input[value=Add]").click();

        assertThat(el("body").text()).contains("all the things");

        $("tbody tr:last-child input[value=Delete]").click();

        assertThat(el("body").text()).doesNotContain("all the things");

        goTo(MULTI_GRANT_BASE_URL + "/info");
        $("#logout").click();
    }
}
