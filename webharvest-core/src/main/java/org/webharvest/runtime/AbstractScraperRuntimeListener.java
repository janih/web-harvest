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

import java.util.Map;

import org.webharvest.runtime.processors.Processor;

/**
 * Abstract class allowing inheriting subclasses to do not implement all
 * {@link ScraperRuntimeListener} methods, just these in which subclass is
 * really interested.
 * <p/>
 * This class is a temporary solution - we need more flexible approach, such as
 * an event sourcing!
 */
public abstract class AbstractScraperRuntimeListener implements
        ScraperRuntimeListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecutionStart(final Scraper scraper) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecutionPaused(final Scraper scraper) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecutionContinued(final Scraper scraper) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewProcessorExecution(final Scraper scraper,
            final Processor processor) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecutionEnd(final Scraper scraper) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProcessorExecutionFinished(final Scraper scraper,
            final Processor processor, final Map properties) {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecutionError(final Scraper scraper, final Exception e) {
        // nothing
    }
}
