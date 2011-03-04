package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;

public class FrankMamaPlugin extends WebHarvestPlugin {

    @Override
    public String getName() {
        return "cool-mama";
    }

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        return new NodeVariable("FRANKMAMA: " + evaluateAttribute("name", scraper));
    }
}
