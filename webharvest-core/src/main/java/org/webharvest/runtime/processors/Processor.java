package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

// TODO Add javadoc
public interface Processor<TDef extends IElementDef> {

    /**
     * Sets appropriate element definition to the processor.
     *
     * @deprecated Find better solution to create processor in valid state
     * @param elementDef
     *            the element definition
     */
    @Deprecated
    void setElementDef(TDef elementDef);

    TDef getElementDef();

    // TODO Add javadoc
    Variable run(Scraper scraper, DynamicScopeContext context)
            throws InterruptedException;

}
