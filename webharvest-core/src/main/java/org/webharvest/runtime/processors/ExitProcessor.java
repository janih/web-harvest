package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.ExitDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Exit processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "exit", validAttributes = { "id", "condition", "message" },
        definitionClass = ExitDef.class)
public class ExitProcessor extends AbstractProcessor<ExitDef> {

    public Variable execute(Scraper scraper, DynamicScopeContext context) {
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
            LOG.info("Configuration exited: {}", message);
        }

        return EmptyVariable.INSTANCE;
    }

}
