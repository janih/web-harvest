package org.webharvest.ioc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.xml.sax.InputSource;

//TODO Add documentation
public interface HarvesterFactory {

    // TODO Add documentation
    Harvester create(URL config, HarvestLoadCallback callback)
            throws IOException;

    // TODO Add documentation
    Harvester create(String config, HarvestLoadCallback callback)
            throws FileNotFoundException;

    // TODO Add documentation
    Harvester create(InputSource config, HarvestLoadCallback callback);

}
