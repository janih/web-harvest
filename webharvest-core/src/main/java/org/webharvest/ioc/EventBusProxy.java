package org.webharvest.ioc;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

// TODO Missing documentation
public final class EventBusProxy implements MessagePublisher {

    private final Provider<EventBus> provider;

    // TODO Missing documentation
    // TODO Check provider against null pointer
    @Inject
    public EventBusProxy(final Provider<EventBus> provider) {
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final Object event) {
        provider.get().post(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final Object subscriber) {
        provider.get().register(subscriber);
    }

}
