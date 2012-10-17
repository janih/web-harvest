package org.webharvest.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.events.ProcessorStartEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public final class FakeNotifier {

    private static final Logger LOG = LoggerFactory.
            getLogger(FakeNotifier.class);

    private EventBus eventBus;

    @Inject
    public FakeNotifier(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void sendEvent() {
        eventBus.post(new ProcessorStartEvent(null));
        LOG.info("Notification sent {}", eventBus);
    }

}
