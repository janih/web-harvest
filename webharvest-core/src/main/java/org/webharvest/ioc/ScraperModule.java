package org.webharvest.ioc;

import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeListener;
import com.google.inject.spi.TypeEncounter;;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().
                build(ScraperFactory.class));
        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class).in(Singleton.class); //FIXME: do we need custom scope (Scraper's lifetime scope)?

        // FIXME rbala EventBusTypeListener will create instance of event bus
        //bindListener(Matchers.any(), new EventBusTypeListener());

        final EventBus eventBus = new EventBus();
        bind(EventBus.class).toInstance(eventBus);
        bindListener(Matchers.any(), new TypeListener() {
               @Override
               public <I> void hear(@SuppressWarnings("unused") final TypeLiteral<I> typeLiteral, final TypeEncounter<I> typeEncounter) {
                   typeEncounter.register(new InjectionListener<I>() {
                       @Override public void afterInjection(final I instance) {
                           eventBus.register(instance);
                       }
                   });
               }
            });


    }

}
