package io.pivotal.cf.identity.samples.journeys;

import org.fluentlenium.adapter.junit.FluentTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Base class that configures a headless Chrome WebDriver for FluentLenium 5 + Selenium 4.16.
 * Selenium Manager (built into Selenium 4.6+) resolves the matching ChromeDriver automatically.
 */
public abstract class HeadlessChromeTest extends FluentTest {

    @Override
    public WebDriver newWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--headless=new", "--disable-gpu");
        return new ChromeDriver(options);
    }
}
