package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Support for database operations.
 */
public class TokenizePlugin extends WebHarvestPlugin {

    public String getName() {
        return "tokenize";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        String delimiters = evaluateAttribute("delimiters", scraper);
        if ( delimiters == null || "".equals(delimiters) ) {
            delimiters = "\n\r";
        }
        boolean trimTokens = evaluateAttributeAsBoolean("trimtokens", true, scraper);
        boolean allowWmptyTokens = evaluateAttributeAsBoolean("allowemptytokens", false, scraper);
        String text =  executeBody(scraper, context).toString();

        this.setProperty("Delimiters", delimiters);
        this.setProperty("Trim tokens", trimTokens);
        this.setProperty("Allow empty tokens", allowWmptyTokens);

        String tokens[] = CommonUtil.tokenize(text, delimiters, trimTokens, allowWmptyTokens);

        ListVariable listVariable = new ListVariable();
        for (String token: tokens) {
            listVariable.addVariable(new NodeVariable(token));
        }

        return listVariable;
    }

    public String[] getValidAttributes() {
        return new String[] {
                "delimiters",
                "trimtokens",
                "allowemptytokens"
        };
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("trimtokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        } else if ("allowemptytokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}