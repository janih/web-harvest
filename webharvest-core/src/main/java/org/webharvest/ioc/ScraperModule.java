package org.webharvest.ioc;

import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        final ScraperScope scraperScope = new ScraperScope();
        final EventBus eventBus = new EventBus();
        install(new FactoryModuleBuilder().
                build(ScraperFactory.class));
        bindScope(ScrapingScope.class, scraperScope);
        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class)
            .in(scraperScope);
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new EventBusTypeListener(eventBus));
        // Make our scope instance injectable
        bind(ScraperScope.class).annotatedWith(Names.named("scraperScope")).
            toInstance(scraperScope);
    }

}
