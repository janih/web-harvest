package org.webharvest.ioc;

import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    private final EventBus eventBus = new EventBus();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().
                build(ScraperFactory.class));
        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class).
            in(Singleton.class); //FIXME: do we need custom scope (Scraper's lifetime scope)?
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new EventBusTypeListener(eventBus));
    }

}
