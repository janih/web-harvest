package org.webharvest.runtime.processors;

import org.webharvest.definition.AbstractElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.util.concurrent.Callable;

/**
 * Processor which executes only body and returns variables list.
 */
public class BodyProcessor extends AbstractProcessor<AbstractElementDef> {

    public BodyProcessor(AbstractElementDef elementDef) {
        super(elementDef);
    }

    public Variable execute(final Scraper scraper, final DynamicScopeContext context) throws InterruptedException {
        final IElementDef[] defs = elementDef.getOperationDefs();

        if (defs.length == 0) {
            return CommonUtil.createVariable(elementDef.getBodyText());
        }
        if (defs.length == 1) {
            return context.executeWithinNewContext(new Callable<Variable>() {
                @Override
                public Variable call() throws Exception {
                    return CommonUtil.createVariable(ProcessorResolver.createProcessor(defs[0]).run(scraper, context));
                }
            });
        }

        return context.executeWithinNewContext(new Callable<Variable>() {
            @Override
            public Variable call() throws Exception {
                final ListVariable result = new ListVariable();
                for (IElementDef def : defs) {
                    final Variable variable = ProcessorResolver.createProcessor(def).run(scraper, context);
                    if (!variable.isEmpty()) {
                        result.addVariable(variable);
                    }
                }
                return result;
            }
        });
    }
}