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

public final class DefaultHarvest implements Harvest {

    private final HarvesterFactory harvestFactory;

    @Inject
    public DefaultHarvest(final HarvesterFactory harvestFactory) {
        this.harvestFactory = harvestFactory;
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
            final HarvestLoadCallback callback)
                throws FileNotFoundException {
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

}
