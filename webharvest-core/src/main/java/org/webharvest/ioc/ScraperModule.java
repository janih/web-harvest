package org.webharvest.ioc;

import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    private static final Scope SCRAPER_SCOPE = new ScraperScope();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindScope(ScrapingScope.class, SCRAPER_SCOPE);
        // Make our scope instance injectable
        bind(ScraperScope.class).toInstance((ScraperScope) SCRAPER_SCOPE);

        final EventBus eventBus = new EventBus();
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new EventBusTypeListener(eventBus));

        install(new FactoryModuleBuilder().build(ScraperFactory.class));

        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class)
            .in(ScrapingScope.class);
    }
}
