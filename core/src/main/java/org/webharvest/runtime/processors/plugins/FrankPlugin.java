package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;

public class FrankPlugin extends WebHarvestPlugin {

    @Override
    public String getName() {
        return "cool";
    }

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        return executeBody(scraper, context);
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            FrankMamaPlugin.class
        };
    }

    @Override
    public String[] getValidAttributes() {
        return new String[] {"v:*", "p:*", "name"};
    }

}
