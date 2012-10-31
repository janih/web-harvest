package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.Harvest;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.events.EventHandler;
import org.webharvest.events.EventSink;
import org.webharvest.events.HandlerHolder;
import org.webharvest.events.HarvesterEvent;
import org.webharvest.ioc.HarvesterFactory;
import org.xml.sax.InputSource;

import com.google.inject.Inject;

/**
 * Default implementation of {@link Harvest} interface.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Harvest
 */
public final class DefaultHarvest implements Harvest {

    private final HarvesterFactory harvestFactory;

    private final HandlerHolder handlerHolder;

    private EventSink eventSink;

    /**
     * Default class constructor specifying {@link HarvesterFactory},
     * {@link HandlerHolder} and {@link EventSink} that are expected to be Guice
     * injected.
     *
     * @param harvestFactory
     *            reference to factory capable to produce {@link Harvester}
     *            objects.
     * @param handlerHolder
     *            reference to object storing all registered event handlers.
     * @param eventSink
     *            reference to event bus facade.
     */
    @Inject
    public DefaultHarvest(final HarvesterFactory harvestFactory,
            final HandlerHolder handlerHolder, final EventSink eventSink) {
        this.harvestFactory = harvestFactory;
        this.handlerHolder = handlerHolder;
        this.eventSink = eventSink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final URL config,
            final HarvestLoadCallback callback) throws IOException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final String config,
            final HarvestLoadCallback callback) throws FileNotFoundException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final InputSource config,
            final HarvestLoadCallback callback) {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventHandler(final EventHandler<?> handler) {
        handlerHolder.register(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends HarvesterEvent> void postEvent(final E event) {
        eventSink.publish(event);
    }

}
