package org.webharvest.events;

import org.webharvest.runtime.WebScraper;

public final class ScraperExecutionPausedEvent {

    private final WebScraper scraper;

    public ScraperExecutionPausedEvent(final WebScraper scraper) {
        this.scraper = scraper;
    }

    public WebScraper getScraper() {
        return scraper;
    }

}
