package org.webharvest.ioc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.xml.sax.InputSource;

// TODO Add javadoc
@Deprecated
public interface ScraperFactory {

    // TODO Add javadoc
    ScraperConfiguration createConfiguration(InputSource stream);

    // TODO Add javadoc
    ScraperConfiguration createConfiguration(File file) throws FileNotFoundException;

    // TODO Add javadoc
    ScraperConfiguration createConfiguration(String path)
            throws FileNotFoundException;

    // TODO Add javadoc
    ScraperConfiguration createConfiguration(URL url) throws IOException;

    // TODO Add javadoc
    Scraper createScraper(ScraperConfiguration configuration, String workingDir);

}
