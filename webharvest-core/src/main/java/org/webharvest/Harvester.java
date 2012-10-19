package org.webharvest;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.WebScraper;

public interface Harvester {

    // TODO Add documentation
    DynamicScopeContext execute(ContextInitCallback callback);

    // TODO Remove ass soon as possible
    @Deprecated
    WebScraper getScraper();

    // TODO Add documentation
    interface ContextInitCallback {

        // TODO Add documentation
        void onSuccess(DynamicScopeContext context);

    }

    @Deprecated
    void setDebug(boolean debug);

}
