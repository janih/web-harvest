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

// TODO Missing documentation
// TODO Missing unit test
public final class DefaultHarvest implements Harvest {

    private final HarvesterFactory harvestFactory;

    private final HandlerHolder handlerHolder;

    @Inject
    private EventSink eventSink;

    // TODO Missing documentation
    // TODO Missing unit test
    @Inject
    public DefaultHarvest(final HarvesterFactory harvestFactory,
            final HandlerHolder handlerHolder) {
        this.harvestFactory = harvestFactory;
        this.handlerHolder = handlerHolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Harvester getHarvester(final URL config,
            final HarvestLoadCallback callback) throws IOException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Harvester getHarvester(final String config,
            final HarvestLoadCallback callback) throws FileNotFoundException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Harvester getHarvester(final InputSource config,
            final HarvestLoadCallback callback) {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void addEventHandler(final EventHandler<?> handler) {
        handlerHolder.register(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public <E extends HarvesterEvent>  void postEvent(final E event) {
        eventSink.publish(event);
    }

}
