package org.webharvest.events;

import org.webharvest.runtime.processors.Processor;

// TODO Add javadoc
// TODO Add unit test
// FIXME Do we need overwritten hashcode and equals?
public final class ProcessorStartEvent implements ProcessorEvent {

    private final Processor processor;

    // TODO Add javadoc
    // TODO Add unit test
    // TODO Protect against null
    public ProcessorStartEvent(final Processor processor) {
        this.processor = processor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Processor getProcessor() {
        return processor;
    }

}
