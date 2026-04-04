package com.aisleon.common.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaywrightConfig {

    @Value("${PLAYWRIGHT_HEADLESS:true}")
    private boolean headless;

    private Playwright playwright;
    private Browser browser;

    @Bean
    public Playwright playwright() {
        this.playwright = Playwright.create();
        return this.playwright;
    }

    @Bean
    public Browser playwrightBrowser(Playwright playwright) {
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless)
        );
        return this.browser;
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
