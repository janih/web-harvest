/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */

package org.webharvest.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.webharvest.Harvest;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.events.EventHandler;
import org.webharvest.events.EventSink;
import org.webharvest.events.HandlerHolder;
import org.webharvest.events.HarvesterEvent;
import org.webharvest.ioc.HarvesterFactory;
import org.xml.sax.InputSource;

import com.google.inject.Inject;

/**
 * Default implementation of {@link Harvest} interface.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Harvest
 */
public final class DefaultHarvest implements Harvest {

    private final HarvesterFactory harvestFactory;

    private final HandlerHolder handlerHolder;

    private EventSink eventSink;

    /**
     * Default class constructor specifying {@link HarvesterFactory},
     * {@link HandlerHolder} and {@link EventSink} that are expected to be Guice
     * injected.
     *
     * @param harvestFactory
     *            reference to factory capable to produce {@link Harvester}
     *            objects.
     * @param handlerHolder
     *            reference to object storing all registered event handlers.
     * @param eventSink
     *            reference to event bus facade.
     */
    @Inject
    public DefaultHarvest(final HarvesterFactory harvestFactory,
            final HandlerHolder handlerHolder, final EventSink eventSink) {
        this.harvestFactory = harvestFactory;
        this.handlerHolder = handlerHolder;
        this.eventSink = eventSink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final URL config,
            final HarvestLoadCallback callback) throws IOException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final String config,
            final HarvestLoadCallback callback) throws FileNotFoundException {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester(final InputSource config,
            final HarvestLoadCallback callback) {
        return harvestFactory.create(config, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEventHandler(final EventHandler<?> handler) {
        handlerHolder.register(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends HarvesterEvent> void postEvent(final E event) {
        eventSink.publish(event);
    }

}
