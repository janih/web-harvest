package org.webharvest.runtime;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.events.ProcessorStartEvent;
import org.webharvest.events.ProcessorStopEvent;
import org.webharvest.events.ScraperExecutionContinuedEvent;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.events.ScraperExecutionPausedEvent;
import org.webharvest.events.ScraperExecutionStartEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

// TODO Missing documentation
// TODO Missing unit test
public final class RuntimeListenerAdapter {

    private static final Logger LOG = LoggerFactory
            .getLogger(RuntimeListenerAdapter.class);

    private final List<ScraperRuntimeListener> listeners =
            new LinkedList<ScraperRuntimeListener>();

    private final SubscriberHelper subscriber;

    // TODO Missing documentation
    // TODO Missing unit test
    public RuntimeListenerAdapter() {
        this.subscriber = new SubscriberHelper();
    }

    // TODO Missing documentation
    // TODO Missing unit test
    public void register(final EventBus eventBus) {
        eventBus.register(subscriber);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    public void unregister(final EventBus eventBus) {
        eventBus.unregister(subscriber);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    public void add(final ScraperRuntimeListener listener) {
        listeners.add(listener);
    }

    // TODO Missing documentation
    // TODO Missing unit test
    public void remove(final ScraperRuntimeListener listener) {
        listeners.remove(listener);
    }

    private final class SubscriberHelper {

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ScraperExecutionStartEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onExecutionStart(event.getScraper());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ScraperExecutionPausedEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onExecutionPaused(event.getScraper());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ScraperExecutionContinuedEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onExecutionContinued(event.getScraper());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ScraperExecutionEndEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onExecutionEnd(event.getScraper());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ScraperExecutionErrorEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onExecutionError(event.getScraper(),
                        event.getException());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ProcessorStartEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onNewProcessorExecution(event.getScraper(),
                        event.getProcessor());
            }
            LOG.info("Handled event [{}]", event);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        @Subscribe
        public void handle(final ProcessorStopEvent event) {
            for (final ScraperRuntimeListener listener : listeners) {
                listener.onProcessorExecutionFinished(event.getScraper(),
                        event.getProcessor(), event.getProperties());
            }
            LOG.info("Handled event [{}]", event);
        }

    }

}
