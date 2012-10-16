package org.webharvest.ioc;

import org.webharvest.runtime.Scraper;
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

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindConstant().annotatedWith(WorkingDir.class).to(workingDir);

        bindScope(ScrapingScope.class, SCRAPER_SCOPE);
        // Make our scope instance injectable
        bind(ScraperScope.class).toInstance((ScraperScope) SCRAPER_SCOPE);

        final EventBus eventBus = new EventBus();
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new EventBusTypeListener());

        requestStaticInjection(EventBusTypeListener.Factory.class);

        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class)
            .in(ScrapingScope.class);

        bind(MessagePublisher.class).to(EventBusProxy.class).
            in(Singleton.class);

        install(new FactoryModuleBuilder().
                implement(WebScraper.class, Scraper.class).
                build(ScraperFactory.class));
    }

}
