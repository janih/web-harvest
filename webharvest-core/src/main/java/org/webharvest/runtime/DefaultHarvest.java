package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.Harvest;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.ioc.HarvesterFactory;
import org.xml.sax.InputSource;

import com.google.inject.Inject;

// TODO Missing documentation
// TODO Missing unit test
public final class DefaultHarvest implements Harvest {

    private final HarvesterFactory harvestFactory;

    // TODO Missing documentation
    // TODO Missing unit test
    @Inject
    public DefaultHarvest(final HarvesterFactory harvestFactory) {
        this.harvestFactory = harvestFactory;
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

}
