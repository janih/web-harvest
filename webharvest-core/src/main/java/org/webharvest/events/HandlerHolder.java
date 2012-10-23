package org.webharvest.events;

/**
 * Represents an object that serves purpose as storage of {@link EventHandler}
 * (supporting different types of events).
 * Handlers can be registered but not unregistered.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see EventHandler
 */
public interface HandlerHolder {

    /**
     * Register event handler.
     *
     * @param handler event handler to register.
     */
    // FIXME rbala Do we need a registration handler with unregister method?
    void register(EventHandler<?> handler);

}
