package org.webharvest.events;

import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.processors.Processor;

// TODO Add javadoc
// TODO Add unit test
// FIXME Do we need overwritten hashcode and equals?
public final class ProcessorStartEvent {

    @Deprecated
    private final WebScraper scraper;

    private final Processor processor;

    // TODO Add javadoc
    // TODO Add unit test
    // TODO Protect against null
    public ProcessorStartEvent(final WebScraper scraper,
            final Processor processor) {
        this.scraper = scraper;
        this.processor = processor;
    }

    @Deprecated
    public WebScraper getScraper() {
        return scraper;
    }

    public Processor getProcessor() {
        return processor;
    }

}
