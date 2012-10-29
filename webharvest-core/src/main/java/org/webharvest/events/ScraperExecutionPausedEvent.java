package org.webharvest.events;

import org.webharvest.Harvester;
import org.webharvest.runtime.WebScraper;

public final class ScraperExecutionPausedEvent implements HarvesterEvent{

    private final Harvester harvester;

    public ScraperExecutionPausedEvent(final Harvester harvester) {
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
