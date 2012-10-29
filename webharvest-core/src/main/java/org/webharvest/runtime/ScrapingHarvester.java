package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.ioc.ConfigModule;
import org.webharvest.ioc.ScraperFactory;
import org.webharvest.ioc.Scraping;
import org.xml.sax.InputSource;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

// TODO Add documentation
// TODO Add unit test
// FIXME rbala Can not be final as we put an @Scraping annotation on this
public class ScrapingHarvester implements Harvester {

    private final ScraperFactory scraperFactory;

    private final ScraperConfiguration config;

    // TODO rbala not needed when we finally get rid of getScraper() method
    // FIXME rbala Not thread safe (no synchronization)
    @Deprecated
    private WebScraper scraper;

    @Deprecated
    boolean debug;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala more then 4 parameters
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final URL config,
            @Assisted final HarvestLoadCallback callback) throws IOException {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala more then 4 parameters
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final String config,
            @Assisted final HarvestLoadCallback callback)
            throws FileNotFoundException {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final InputSource config,
            @Assisted final HarvestLoadCallback callback) {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    // TODO Add documentation
    private ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory, final Module module,
            final HarvestLoadCallback loadCallback) {
        this.scraperFactory = scraperFactory;
        this.config = injector.createChildInjector(module).
            getInstance(ScraperConfiguration.class);
        loadCallback.onSuccess(config.getOperations());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Scraping
    public DynamicScopeContext execute(final ContextInitCallback callback) {
        this.scraper = scraperFactory.create(config);
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
        // Return reference to scraper from last call
        return scraper;
    }
}
