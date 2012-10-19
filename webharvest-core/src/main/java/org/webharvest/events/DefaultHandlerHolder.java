package org.webharvest.events;

import java.util.LinkedList;
import java.util.List;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

// TODO Missing documentation
// TODO Missing unit test
public final class DefaultHandlerHolder implements HandlerHolder {

    private final List<EventHandler<?>> handlers =
            new LinkedList<EventHandler<?>>();

    private Provider<EventBus> provider;

    // TODO Missing documentation
    // TODO Missing unit test
    @Inject
    public DefaultHandlerHolder(final Provider<EventBus> provider) {
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void register(final EventHandler<?> handler) {
        handlers.add(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void subscribe() {
        final EventBus eventBus = provider.get();
        for (final EventHandler<?> handler : handlers) {
            eventBus.register(handler);
        }
    }

}
