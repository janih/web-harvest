package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.ioc.ScraperFactory;
import org.xml.sax.InputSource;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

// TODO Add documentation
// TODO Add unit test
public final class ScrapingHarvester implements Harvester {

    private final WebScraper scraper;

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final ScraperFactory scraperFactory,
            @Assisted final URL config,
            @Assisted final HarvestLoadCallback callback) throws IOException {
        this(scraperFactory.create(config), callback);
    }

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final ScraperFactory scraperFactory,
            @Assisted final String config,
            @Assisted final HarvestLoadCallback callback)
            throws FileNotFoundException {
        this(scraperFactory.create(config), callback);
    }

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final ScraperFactory scraperFactory,
            @Assisted final InputSource config,
            @Assisted final HarvestLoadCallback callback) {
        this(scraperFactory.create(config), callback);
    }

    // TODO Add documentation
    private ScrapingHarvester(final WebScraper scraper,
            final HarvestLoadCallback callback) {
        this.scraper = scraper;
        callback.onSuccess(scraper.getConfiguration().getOperations());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynamicScopeContext execute(final ContextInitCallback callback) {
        callback.onSuccess(scraper.getContext());
        scraper.execute();

        return scraper.getContext();
    }

    /**
     * @deprecated Remove as soon as possible
     */
    @Override
    @Deprecated
    public WebScraper getScraper() {
        return scraper;
    }

}
