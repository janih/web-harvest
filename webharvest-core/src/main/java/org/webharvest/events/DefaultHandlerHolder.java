package org.webharvest.events;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.AlreadyBoundException;
import org.webharvest.Harvester;
import org.webharvest.Registry;
import org.webharvest.ScrapingAware;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

// TODO Missing documentation
// TODO Missing unit test
public final class DefaultHandlerHolder implements HandlerHolder,
        ScrapingAware {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultHandlerHolder.class);

    private final List<EventHandler<?>> handlers =
            new LinkedList<EventHandler<?>>();

    private final Registry<Harvester, EventBus> registry;

    private final Provider<EventBus> provider;

    // TODO Missing documentation
    // TODO Missing unit test
    @Inject
    public DefaultHandlerHolder(final Registry<Harvester, EventBus> registry,
            final Provider<EventBus> provider) {
        this.registry = registry;
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
    public void onBeforeScraping(final Harvester harvester) {
        final EventBus eventBus = provider.get();
        for (final EventHandler<?> handler : handlers) {
            eventBus.register(handler);
            LOG.debug("Registered event bus handler [{}]", handler);
        }
        try {
            registry.bind(harvester, eventBus);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void onAfterScraping(final Harvester harvester) {
        final EventBus eventBus = registry.lookup(harvester);
        if (eventBus == null) {
            throw new IllegalStateException("Cound not find event bus");
        }
        for (final EventHandler<?> handler : handlers) {
            eventBus.unregister(handler);
            LOG.debug("Unregistered event bus handler [{}]", handler);
        }
        registry.unbind(harvester);
    }

}
