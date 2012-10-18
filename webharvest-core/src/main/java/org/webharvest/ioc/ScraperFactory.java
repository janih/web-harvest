package org.webharvest.ioc;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.WebScraper;

//TODO Add documentation
public interface ScraperFactory {

    // TODO Add documentation
    // FIXME rbala I'm not convinced this is good idea
    WebScraper create(ScraperConfiguration config);

}
