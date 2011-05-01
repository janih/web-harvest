package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.Variable;

public class FrankPlugin extends WebHarvestPlugin {

    @Override
    public String getName() {
        return "cool";
    }

    @Override
    public Variable executePlugin(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
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
