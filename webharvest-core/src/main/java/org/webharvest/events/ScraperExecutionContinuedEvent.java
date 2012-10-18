package org.webharvest.events;

import org.webharvest.runtime.WebScraper;

public final class ScraperExecutionContinuedEvent {

    private final WebScraper scraper;

    public ScraperExecutionContinuedEvent(final WebScraper scraper) {
        this.scraper = scraper;
    }

    public WebScraper getScraper() {
        return scraper;
    }

}
