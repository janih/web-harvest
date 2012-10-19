package org.webharvest.events;

// TODO Missing documentation
public interface EventHandler<T> {

    // FIXME rbala Implement solution that does not require to declare @Subscribe annotiation
    // TODO Missing documentation
    void handle(T event);

}
