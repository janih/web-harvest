package org.webharvest.runtime;

import java.util.Map;

import org.webharvest.runtime.processors.Processor;

/**
 * @author Vladimir Nikic
 * Date: Apr 20, 2007
 *
 * @deprecated Use event sourcing instead
 *
 */
public interface ScraperRuntimeListener {

    public void onExecutionStart(Scraper scraper);

    public void onExecutionPaused(Scraper scraper);

    public void onExecutionContinued(Scraper scraper);

    public void onNewProcessorExecution(Scraper scraper, Processor processor);

    public void onExecutionEnd(Scraper scraper);

    public void onProcessorExecutionFinished(Scraper scraper, Processor processor, Map properties);

    public void onExecutionError(Scraper scraper, Exception e);

}