package org.webharvest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.events.EventHandler;
import org.xml.sax.InputSource;

// TODO Add documentation
public interface Harvest {

    // TODO Add documentation
    Harvester getHarvester(URL config, HarvestLoadCallback callback)
            throws IOException;

    // TODO Add documentation
    Harvester getHarvester(String config, HarvestLoadCallback callback)
            throws FileNotFoundException;

    // TODO Add documentation
    Harvester getHarvester(InputSource config, HarvestLoadCallback callback);

    void addEventHandler(EventHandler<?> handler);

}
