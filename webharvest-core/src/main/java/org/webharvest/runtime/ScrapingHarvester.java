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

/**
 * Default implementation of {@link Harvester} interface aimed to perform data
 * extraction from remote websites. Its worth to note that web scraping may be
 * against the terms of use of some websites in rather unclear manner. Current
 * implementation serves purpose of a kind of facade for {@link WebScraper}
 * instance that is obtained during invocation of
 * {@link #execute(org.webharvest.Harvester.ContextInitCallback)} method. Class
 * constructors are Guice aware and the dependencies are ment to be
 * automatically created. Unfortunately since the instance of this class is
 * produced by Guice dynamic factory the new instance will get injected its
 * dependencies but itself can not be a dependency to other container objects.
 * When instantiated with a help of Guice it provides additional behavior
 * introduced with {@link Scraping} annotated
 * {@link #execute(org.webharvest.Harvester.ContextInitCallback)} method. In
 * such situation invocation of this method automatically creates execution
 * scope so new instance of {@link WebScraper} object (and most of its
 * dependencies) lives within it.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Harvester
 * @see Injector
 * @see ScraperFactory
 * @see HarvestLoadCallback
 */
// FIXME rbala Can not be final as we put an @Scraping annotation on this
public class ScrapingHarvester implements Harvester {

    private final ScraperFactory scraperFactory;

    private final ScraperConfiguration config;

    // TODO rbala not needed when we finally get rid of getScraper() method
    // FIXME rbala Not thread safe (no synchronization)
    @Deprecated
    private WebScraper scraper;

    /**
     * Class constructor expecting Guice {@link Injector},
     * {@link ScraperFactory}, {@link URL} and {@link HarvestLoadCallback} to be
     * specified.
     *
     * @param injector
     *            Guice {@link Injector}.
     * @param scraperFactory
     *            the {@link WebScraper} factory.
     * @param config
     *            an url to remote configuration.
     * @param callback
     *            reference to a callback that is automatically invoked on
     *            successful load of configuration.
     * @throws IOException
     */
    // FIXME rbala more then 4 parameters
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory, @Assisted final URL config,
            @Assisted final HarvestLoadCallback callback) throws IOException {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    /**
     * Class constructor expecting Guice {@link Injector},
     * {@link ScraperFactory}, configuration file path and
     * {@link HarvestLoadCallback} to be specified.
     *
     * @param injector
     *            Guice {@link Injector}.
     * @param scraperFactory
     *            the {@link WebScraper} factory.
     * @param config
     *            configuration file path.
     * @param callback
     *            reference to a callback that is automatically invoked on
     *            successful load of configuration.
     * @throws IOException
     */
    // FIXME rbala more then 4 parameters
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory, @Assisted final String config,
            @Assisted final HarvestLoadCallback callback)
            throws FileNotFoundException {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    /**
     * Class constructor expecting Guice {@link Injector},
     * {@link ScraperFactory}, {@link URL} and {@link HarvestLoadCallback} to be
     * specified.
     *
     * @param injector
     *            Guice {@link Injector}.
     * @param scraperFactory
     *            the {@link WebScraper} factory.
     * @param config
     *            configuration XML stream.
     * @param callback
     *            reference to a callback that is automatically invoked on
     *            successful load of configuration.
     * @throws IOException
     */
    @AssistedInject
    public ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory,
            @Assisted final InputSource config,
            @Assisted final HarvestLoadCallback callback) {
        this(injector, scraperFactory, new ConfigModule(config), callback);
    }

    private ScrapingHarvester(final Injector injector,
            final ScraperFactory scraperFactory, final Module module,
            final HarvestLoadCallback loadCallback) {
        this.scraperFactory = scraperFactory;
        this.config = injector.createChildInjector(module).getInstance(
                ScraperConfiguration.class);
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
