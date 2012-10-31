package org.webharvest.events;

import org.webharvest.Harvester;
import org.webharvest.runtime.WebScraper;

public final class ScraperExecutionContinuedEvent implements HarvesterEvent {

    private final Harvester harvester;

    public ScraperExecutionContinuedEvent(final Harvester harvester) {
        this.harvester = harvester;
    }

    public WebScraper getScraper() {
        return harvester.getScraper();
    }

    @Override
    public Harvester getHarvester() {
        return harvester;
    }

}
