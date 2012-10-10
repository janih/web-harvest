package org.webharvest.ioc;

import com.google.common.eventbus.EventBus;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link TypeListener} implementation that is responsible for
 * registration of objects managed by Guice in singleton {@link EventBus}.
 * Each newly created object is treated as potential event subscriber.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see TypeListener
 * @see EventBus
 */
public final class EventBusTypeListener implements TypeListener {

    // FIXME rbala I could not find solution that allows to inject event bus as singleton (since type listener is not created by Guice)
    private final EventBus eventBus = new EventBus();

    /**
     * {@inheritDoc}
     */
    @Override
     public <I> void hear(final TypeLiteral<I> typeLiteral,
             final TypeEncounter<I> typeEncounter) {
         typeEncounter.register(new InjectionListener<I>() {
             public void afterInjection(final I i) {
                 eventBus.register(i);
             }
         });
     }

}
