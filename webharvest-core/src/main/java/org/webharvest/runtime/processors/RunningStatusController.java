package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.google.common.util.concurrent.Monitor;

//TODO missing javadoc and unit test
public final class RunningStatusController<TDef extends IElementDef> extends
        AbstractProcessorDecorator<TDef> {

    private final Monitor monitor;
    private final Monitor.Guard runningGuard;

    public RunningStatusController(final Processor<TDef> decoratedProcessor) {
        super(decoratedProcessor);
        this.monitor = InjectorHelper.getInjector().getInstance(Monitor.class);
        this.runningGuard = InjectorHelper.getInjector().
            getInstance(Monitor.Guard.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable run(final Scraper scraper,
            final DynamicScopeContext context)
                throws InterruptedException {
        try {
            monitor.enterWhen(runningGuard);
        } finally {
            monitor.leave();
        }
        return this.decoratedProcessor.run(scraper, context);
    }

}
