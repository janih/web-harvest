package org.webharvest.ioc;

import java.io.File;

import org.webharvest.Harvest;
import org.webharvest.Harvester;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScrapingHarvester;
import org.webharvest.runtime.DefaultHarvest;
import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    private static final Scope SCRAPER_SCOPE = new ScraperScope();

    private final String workingDir;

    private final String configDir;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final String workingDir) {
        this.workingDir = workingDir;
        // Set the current directory
        this.configDir = new File("").getAbsolutePath();
    }

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final String workingDir, final String configDir) {
        this.workingDir = workingDir;
        this.configDir = configDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new PostConstructListener());

        bindConstant().annotatedWith(WorkingDir.class).to(workingDir);
        bindConstant().annotatedWith(ConfigDir.class).to(configDir);

        bindScope(ScrapingScope.class, SCRAPER_SCOPE);
        // Make our scope instance injectable
        bind(ScraperScope.class).toInstance((ScraperScope) SCRAPER_SCOPE);

        bind(EventBus.class).in(Singleton.class);
        bindListener(Matchers.any(), new EventBusTypeListener());

        requestStaticInjection(InjectorHelper.class);

        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class)
            .in(ScrapingScope.class);

        bind(AttributeHolder.class).to(ScopeAttributeHolder.class);

        bind(Harvest.class).to(DefaultHarvest.class).in(Singleton.class);

        install(new FactoryModuleBuilder().
                implement(WebScraper.class, Scraper.class).
                build(ScraperFactory.class));

        install(new FactoryModuleBuilder().
                implement(Harvester.class, ScrapingHarvester.class).
                build(HarvesterFactory.class));

        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Scraping.class),
                new ScrapingInterceptor());
    }

}
