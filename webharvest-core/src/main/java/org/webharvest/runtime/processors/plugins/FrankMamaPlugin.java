package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

public class FrankMamaPlugin extends WebHarvestPlugin {

    @Override
    public String getName() {
        return "cool-mama";
    }

    @Override
    public Variable executePlugin(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
        return new NodeVariable("FRANKMAMA: " + evaluateAttribute("name", scraper));
    }

    @Override
    public String[] getValidAttributes() {
        return new String[] {"name"};
    }
}
