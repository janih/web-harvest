package org.webharvest.ioc;

import java.io.IOException;

import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.ScraperConfiguration;

@Deprecated
// TODO rbala remove when come up solution replacing ScraperConfiguration
public interface ConfigFactory {

    ScraperConfiguration create(ConfigSource config) throws IOException;

}
