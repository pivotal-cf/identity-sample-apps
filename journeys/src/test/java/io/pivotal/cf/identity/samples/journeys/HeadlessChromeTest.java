package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base class that configures a headless Chrome WebDriver for FluentLenium 5 + Selenium 4.16.
 * Selenium Manager (built into Selenium 4.6+) resolves the matching ChromeDriver automatically.
 *
 * If a selenium.remote.url system property/env var is set (e.g. the docker-compose harness's
 * selenium/standalone-chrome service), a RemoteWebDriver is used instead of a local ChromeDriver.
 */
public abstract class HeadlessChromeTest extends FluentTest {

    @Override
    public WebDriver newWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--headless=new", "--disable-gpu");
        String remoteUrl = System.getProperty("selenium.remote.url", System.getenv("SELENIUM_REMOTE_URL"));
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            // UAA unconditionally marks its session cookie Secure (see UaaSessionConfig#uaaCookieSerializer).
            // Chrome's "localhost is a secure context" exception (used by the bare-metal Concourse flow, which
            // addresses everything via http://localhost:<port>) doesn't apply to the compose network's service
            // names (uaa, authcode, ...), so the Secure cookie would otherwise be silently dropped, breaking the
            // OAuth2 login session. This flag replicates that exception for exactly those origins.
            options.addArguments("--unsafely-treat-insecure-origin-as-secure="
                    + "http://uaa:8080,http://authcode:8888,http://client-credentials:8887,"
                    + "http://authcode-client-credentials:8890,http://resource-server:8889");
            try {
                return new RemoteWebDriver(new URL(remoteUrl), options);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
        return new ChromeDriver(options);
    }
}
