package org.webharvest.runtime;


// TODO Missing documentation
public interface WebScraper {

    // TODO Missing documentation
    DynamicScopeContext getContext();

    // TODO Missing documentation
    int getStatus();

    // TODO Missing documentation
    void execute();

    @Deprecated
    void informListenersAboutError(Exception e);

    @Deprecated
    void stopExecution();

    @Deprecated
    void pauseExecution();

    @Deprecated
    String getMessage();
}
