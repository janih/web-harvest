package org.webharvest.events;

/**
 * Represents an object that is handler for particular type of event.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <T> Handler's supported type of event.
 */
public interface EventHandler<T> {

    /**
     * Handle event of supported type..
     *
     * @param event event to handle.
     */
    // FIXME rbala Implement solution that does not require to declare @Subscribe annotiation
    void handle(T event);

}
