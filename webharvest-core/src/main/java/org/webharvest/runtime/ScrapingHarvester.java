package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.ioc.ConfigDir;
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
public class ScrapingHarvester implements Harvester {

    private final ScraperFactory scraperFactory;

    private final ScraperConfiguration config;

    // TODO rbala not needed when we finally get rid of getScraper() method
    private WebScraper scraper;

    @Deprecated
    private List<ScraperRuntimeListener> listeners =
            new LinkedList<ScraperRuntimeListener>();

    @Deprecated
    boolean debug;

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final URL config,
            @Assisted final HarvestLoadCallback callback,
            @ConfigDir final String configDir) throws IOException {
        this(injector, scraperFactory, new ConfigModule(config), callback,
                configDir);
    }

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final String config,
            @Assisted final HarvestLoadCallback callback,
            @ConfigDir final String configDir)
            throws FileNotFoundException {
        this(injector, scraperFactory, new ConfigModule(config), callback,
                configDir);
    }

    // TODO Add documentation
    // TODO Add unit test
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final InputSource config,
            @Assisted final HarvestLoadCallback callback,
            @ConfigDir final String configDir) {
        this(injector, scraperFactory, new ConfigModule(config), callback,
                configDir);
    }

    // TODO Add documentation
    private ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory, final Module module,
            final HarvestLoadCallback loadCallback,
            final String configDir) {
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

        for (final ScraperRuntimeListener listener : listeners) {
            scraper.addRuntimeListener(listener);
        }

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

    @Override
    @Deprecated
    public void addRuntimeListener(final ScraperRuntimeListener listener) {
        listeners.add(listener);
        if (scraper != null) {
            scraper.addRuntimeListener(listener);
        }
    }

    @Override
    @Deprecated
    public void removeRuntimeListener(final ScraperRuntimeListener listener) {
        listeners.remove(listener);
        if (scraper != null) {
            scraper.removeRuntimeListener(listener);
        }
    }

    @Override
    @Deprecated
    public void setDebug(boolean debug) {
        this.debug = debug;
        if (scraper != null) {
            scraper.setDebug(debug);
        }
    }

}
