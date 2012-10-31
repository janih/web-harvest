/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

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

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.events.ScraperExecutionContinuedEvent;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.events.ScraperExecutionPausedEvent;
import org.webharvest.events.ScraperExecutionStartEvent;
import org.webharvest.ioc.ContextFactory;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.ProcessorResolver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Basic runtime class.
 */
public class Scraper implements WebScraper {

    private static final Logger LOG = LoggerFactory.getLogger(Scraper.class);

    public static final int STATUS_READY = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_EXIT = 6;

    @Inject
    private EventBus eventBus;

    @Inject
    private ContextFactory contextFactory;

    private ScraperConfiguration configuration;
    private DynamicScopeContext context;

    private volatile int status = STATUS_READY;
    private String message = null;

    /**
     * Constructor.
     *
     * @param configuration
     * @deprecated as public constructor make it private.
     */
    @AssistedInject
    public Scraper(@Assisted final ScraperConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Initializes {@link DynamicScopeContext} - we need to do it after all are
     * dependencies are injected. Also, we need to pass to the factory namespace
     * URI of the configuration being executed (it is required in order to
     * instantiate appropriate {@link DynamicScopeContext} implementation).
     */
    @PostConstruct
    public void initContext() {
        this.context = contextFactory.create(configuration.getNamespaceURI());
    }

    public void execute() {
        long startTime = System.currentTimeMillis();

        this.setStatus(STATUS_RUNNING);

        // inform all listeners that execution is just about to start
        eventBus.post(new ScraperExecutionStartEvent(this));

        try {
            final Processor processor = ProcessorResolver.createProcessor(
                    configuration.getRootElementDef());
            if (processor != null) {
                processor.run(this, context);
            }
        } catch (InterruptedException e) {
            setStatus(STATUS_STOPPED);
            Thread.currentThread().interrupt();
        }

        if (this.status == STATUS_RUNNING) {
            this.setStatus(STATUS_FINISHED);
        }

        // inform all listeners that execution is finished
        eventBus.post(new ScraperExecutionEndEvent(this));

        if (LOG.isInfoEnabled()) {
            if (this.status == STATUS_FINISHED) {
                LOG.info("Configuration executed in {} ms.",
                        (System.currentTimeMillis() - startTime));
            } else if (this.status == STATUS_STOPPED) {
                LOG.info("Configuration stopped!");
            }
        }
    }

    public DynamicScopeContext getContext() {
        return context;
    }

    public synchronized int getStatus() {
        return status;
    }

    private synchronized void setStatus(int status) {
        this.status = status;
    }

    public void stopExecution() {
        setStatus(STATUS_STOPPED);
    }

    public void exitExecution(String message) {
        setStatus(STATUS_EXIT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Subscribe
    public void pauseExecution(final ScraperExecutionPausedEvent event) {
        if (this.status == STATUS_RUNNING) {
            setStatus(STATUS_PAUSED);

            // inform al listeners that execution is paused
           // eventBus.post(new ScraperExecutionPausedEvent(this));
        }
    }

    @Subscribe
    public void continueExecution(final ScraperExecutionContinuedEvent event) {
        if (this.status == STATUS_PAUSED) {
            setStatus(STATUS_RUNNING);

            // inform al listeners that execution is continued
            // eventBus.post(new ScraperExecutionContinuedEvent(this));
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        setStatus(STATUS_ERROR);

        // inform al listeners that execution is continued
        eventBus.post(new ScraperExecutionErrorEvent(this, e));
    }

}
