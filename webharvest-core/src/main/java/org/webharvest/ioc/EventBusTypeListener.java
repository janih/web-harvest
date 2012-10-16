package org.webharvest.ioc;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
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
// FIXME rbala Is it necessary to create unit test?
public final class EventBusTypeListener implements TypeListener {

    private static final Logger LOG = LoggerFactory.
        getLogger(EventBusTypeListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
     public <I> void hear(final TypeLiteral<I> typeLiteral,
             final TypeEncounter<I> typeEncounter) {

        for (final Method method : typeLiteral.getRawType().
                getDeclaredMethods()) {
            // If one of the class's methods is annotated to receive events then
            // register it to event bus
            if (method.isAnnotationPresent(Subscribe.class)) {
                typeEncounter.register(new InjectionListener<I>() {
                    public void afterInjection(final I i) {
                        Factory.get().register(i);
                    }
                });
            }
        }
     }

    public static class Factory {

        @Inject private static Provider<EventBus> provider;

        public static EventBus get() {
            return provider.get();
        }

    }

}
