package org.webharvest.runtime.processors;

import org.webharvest.definition.AbstractElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.util.concurrent.Callable;

/**
 * Processor which executes only body and returns variables list.
 */
public class BodyProcessor extends AbstractProcessor<AbstractElementDef> {

    public Variable execute(final Scraper scraper,
            final DynamicScopeContext context) throws InterruptedException {
        final IElementDef[] defs = elementDef.getOperationDefs();

        if (defs.length == 0) {
            return CommonUtil.createVariable(elementDef.getBodyText());
        }
        if (defs.length == 1) {
            return context.executeWithinNewContext(new Callable<Variable>() {
                @Override
                public Variable call() throws Exception {
                    return CommonUtil.createVariable(ProcessorResolver
                            .createProcessor(defs[0]).run(scraper, context));
                }
            });
        }

        return context.executeWithinNewContext(new Callable<Variable>() {
            @Override
            public Variable call() throws Exception {
                final ListVariable result = new ListVariable();
                for (IElementDef def : defs) {
                    final Variable variable = ProcessorResolver
                            .createProcessor(def).run(scraper, context);
                    if (!variable.isEmpty()) {
                        result.addVariable(variable);
                    }
                }
                return result.isEmpty() ? EmptyVariable.INSTANCE : result
                        .getList().size() == 1 ? CommonUtil
                        .createVariable(result.get(0)) : result;
            }
        });
    }

    /**
     * A builder responsible for creating instance of {@link BodyProcessor} and
     * completing it with appropriate {@link AbstractElementDef}.
     *
     * @author mczapiewski
     * @since 2.1-SNAPSHOT
     * @version %I%, %G%
     */
    public static final class Builder {

        private final AbstractElementDef elementDef;

        /**
         * Default builder constructor which accepts {@link AbstractElementDef}
         * for {@link BodyProcessor}. Specified element definition should not be
         * null.
         *
         * @param elementDef
         *            an instance of {@link AbstractElementDef}
         */
        public Builder(final AbstractElementDef elementDef) {
            this.elementDef = elementDef;
        }

        /**
         * Returns an instance of {@link BodyProcessor} which is completed with
         * element definition.
         *
         * @return an instance of {@link BodyProcessor}
         */
        public BodyProcessor build() {
            final BodyProcessor processor = new BodyProcessor();
            processor.setElementDef(elementDef);
            return processor;
        }

    }
}