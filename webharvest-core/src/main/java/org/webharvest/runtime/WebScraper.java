package org.webharvest.runtime;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.web.HttpClientManager;

// TODO Missing documentation
public interface WebScraper {

    // TODO Missing documentation
    DynamicScopeContext getContext();

    // TODO Missing documentation
    int getStatus();

    // TODO Missing documentation
    void execute();

    @Deprecated
    ScraperConfiguration getConfiguration();

    @Deprecated
    ScriptEngineFactory getScriptEngineFactory();

    @Deprecated
    void setDebug(boolean debug);

    @Deprecated
    HttpClientManager getHttpClientManager();

    @Deprecated
    void addRuntimeListener(ScraperRuntimeListener listener);

    @Deprecated
    void removeRuntimeListener(ScraperRuntimeListener listener);

    @Deprecated
    void informListenersAboutError(Exception e);

    @Deprecated
    void stopExecution();

    @Deprecated
    void pauseExecution();

}
