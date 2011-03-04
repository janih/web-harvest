package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;

public class DavidPlugin extends WebHarvestPlugin {

    @Override
    public String getName() {
        return "cool";
    }

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        return new NodeVariable("David: " + evaluateAttribute("name", scraper));
    }

    @Override
    public String[] getValidAttributes() {
        return new String[] {"name"};
    }

    @Override
    public String[] getRequiredAttributes() {
        return new String[] {"name"};
    }

}
