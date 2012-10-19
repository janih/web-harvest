package org.webharvest.events;

//TODO Missing documentation
public interface HandlerHolder {

    // TODO Missing documentation
    // FIXME rbala Do we need a registration handler with unregister method?
    void register(EventHandler<?> handler);

    // TODO Missing documentation
    void subscribe();

}
