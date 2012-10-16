package org.webharvest.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.events.ProcessorStartEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public final class FakeNotifier {

    private static final Logger LOG = LoggerFactory.
            getLogger(FakeNotifier.class);

    private MessagePublisher eventBus;

    @Inject
    public FakeNotifier(final MessagePublisher eventBus) {
        this.eventBus = eventBus;
    }

    public void sendEvent() {
        eventBus.publish(new ProcessorStartEvent(null));
        LOG.info("Notification sent {}", eventBus);
    }

    @Subscribe
    public void handleProcessorStartEvent(final ProcessorStartEvent event) {
        LOG.info("Received en event!!! {}", event);
    }

}
