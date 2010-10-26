package org.webharvest.runtime.processors;

import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Processor which executes only body and returns variables list.
 */
public class BodyProcessor extends BaseProcessor<BaseElementDef> {

    public BodyProcessor(BaseElementDef elementDef) {
        super(elementDef);
    }

    public Variable execute(Scraper scraper, ScraperContext context) throws InterruptedException {
        final IElementDef[] defs = elementDef.getOperationDefs();

        if (defs.length == 0) {
            return CommonUtil.createVariable(elementDef.getBodyText());
        }
        if (defs.length == 1) {
            return CommonUtil.createVariable(ProcessorResolver.createProcessor(defs[0]).run(scraper, context));
        }

        final ListVariable result = new ListVariable();
        for (IElementDef def : defs) {
            result.addVariable(ProcessorResolver.createProcessor(def).run(scraper, context));
        }
        return result;
    }
}