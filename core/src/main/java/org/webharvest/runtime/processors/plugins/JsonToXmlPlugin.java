package org.webharvest.runtime.processors.plugins;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Converter from JSON to XML
 */
public class JsonToXmlPlugin extends WebHarvestPlugin {

    private final static String ATTR_ROOT_TAG_NAME = "tag";

    @Override
    public String getName() {
        return "json-to-xml";
    }

    @Override
    public String[] getValidAttributes() {
        return new String[]{ATTR_ROOT_TAG_NAME};
    }

    @Override
    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        try {
            return new NodeVariable(XML.toString(
                    new JSONObject(executeBody(scraper, context).toString()),
                    evaluateAttribute(ATTR_ROOT_TAG_NAME, scraper)));
        } catch (JSONException e) {
            throw new PluginException("Error converting JSON to XML: " + e.getMessage());
        }
    }

}