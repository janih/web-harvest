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

    public void onExecutionStart(WebScraper scraper);

    public void onExecutionPaused(WebScraper scraper);

    public void onExecutionContinued(WebScraper scraper);

    public void onNewProcessorExecution(WebScraper scraper, Processor processor);

    public void onExecutionEnd(WebScraper scraper);

    public void onProcessorExecutionFinished(WebScraper scraper, Processor processor, Map properties);

    public void onExecutionError(WebScraper scraper, Exception e);

}