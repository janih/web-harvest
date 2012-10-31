package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;

//TODO missing javadoc and unit test
public abstract class AbstractProcessorDecorator<TDef extends IElementDef>
    implements Processor<TDef> {

    protected final Processor<TDef> decoratedProcessor;

    public AbstractProcessorDecorator(
            final Processor<TDef> decoratedProcessor) {
        if (decoratedProcessor == null) {
            throw new IllegalArgumentException(
                    "Decorated processor is required.");
        }
        this.decoratedProcessor = decoratedProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElementDef(final TDef elementDef) {
        this.decoratedProcessor.setElementDef(elementDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TDef getElementDef() {
        return this.decoratedProcessor.getElementDef();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Processor getParentProcessor() {
        return this.decoratedProcessor.getParentProcessor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentProcessor(final Processor parentProcessor) {
        this.decoratedProcessor.setParentProcessor(parentProcessor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRunningLevel() {
        return this.decoratedProcessor.getRunningLevel();
    }

}
