package org.webharvest.ioc;

import java.net.URL;

import org.webharvest.definition.ScraperConfiguration;
import org.xml.sax.InputSource;

@Deprecated
// TODO rbala remove when come up solution replacing ScraperConfiguration
public interface ConfigFactory {

    ScraperConfiguration create(URL config);

    ScraperConfiguration create(String config);

    ScraperConfiguration create(InputSource config);

}
