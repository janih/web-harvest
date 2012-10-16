package org.webharvest.ioc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.runtime.Scraper;
import org.xml.sax.InputSource;

//TODO Add documentation
public interface ScraperFactory {

    // TODO Add documentation
    // FIXME rbala I'm not convinced this is good idea
    Scraper create(URL config) throws IOException;

    // TODO Add documentation
    // FIXME rbala I'm not convinced this is good idea
    Scraper create(String config) throws FileNotFoundException;

    // TODO Add documentation
    // FIXME rbala I'm not convinced this is good idea
    Scraper create(InputSource config);

}
