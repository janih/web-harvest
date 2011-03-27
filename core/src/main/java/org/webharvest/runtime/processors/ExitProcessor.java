package org.webharvest.runtime.processors;

import org.webharvest.definition.ExitDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Exit processor.
 */
public class ExitProcessor extends AbstractProcessor<ExitDef> {

    public ExitProcessor(ExitDef exitDef) {
        super(exitDef);
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String condition = BaseTemplater.evaluateToString(elementDef.getCondition(), null, scraper);
        if (condition == null || "".equals(condition)) {
            condition = "true";
        }

        if (CommonUtil.isBooleanTrue(condition)) {
            String message = BaseTemplater.evaluateToString(elementDef.getMessage(), null, scraper);
            if (message == null) {
                message = "";
            }
            scraper.exitExecution(message);
            if (scraper.getLogger().isInfoEnabled()) {
                scraper.getLogger().info("Configuration exited: " + message);
            }
        }

        return EmptyVariable.INSTANCE;
    }

}
