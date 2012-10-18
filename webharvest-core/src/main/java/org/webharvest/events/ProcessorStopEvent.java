package org.webharvest.events;

import java.util.Map;

import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.processors.Processor;

//TODO Add javadoc
//TODO Add unit test
//FIXME Do we need overwritten hashcode and equals?
public final class ProcessorStopEvent {

    @Deprecated
    private final WebScraper scraper;

    private final Processor processor;

    private final Map properties;

    // TODO Add javadoc
    // TODO Add unit test
    // TODO Protect against null
    public ProcessorStopEvent(final WebScraper scraper,
            final Processor processor, final Map properties) {
        this.scraper = scraper;
        this.processor = processor;
        this.properties = properties;
    }

    @Deprecated
    public WebScraper getScraper() {
        return scraper;
    }

    public Processor getProcessor() {
        return processor;
    }

    public Map getProperties() {
        return properties;
    }

}
