package org.webharvest.runtime.processors;

import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Processor which executes only body and returns variables list.
 */
public class BodyProcessor extends BaseProcessor<BaseElementDef> {

    public BodyProcessor(BaseElementDef elementDef) {
        super(elementDef);
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        IElementDef[] defs = elementDef.getOperationDefs();
        ListVariable result = new ListVariable();

        if (defs.length > 0) {
            for (IElementDef def : defs) {
                BaseProcessor processor = ProcessorResolver.createProcessor(def);
                result.addVariable(processor.run(scraper, context));
            }
        } else {
            result.addVariable(new NodeVariable(elementDef.getBodyText()));
        }

        // todo: why always list ???
        return result;
    }

}