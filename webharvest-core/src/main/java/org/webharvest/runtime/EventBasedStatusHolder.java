package org.webharvest.runtime;

import org.webharvest.events.ScraperExecutionContinuedEvent;
import org.webharvest.events.ScraperExecutionExitEvent;
import org.webharvest.events.ScraperExecutionPausedEvent;
import org.webharvest.events.ScraperExecutionStoppedEvent;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Monitor;
import com.google.inject.Inject;

/**
 * {@link StatusHolder} implementation which bases on Scraper's events. It
 * reacts on {@link ScraperExecutionPausedEvent} and
 * {@link ScraperExecutionContinuedEvent} and changes processing status if
 * configuration's processing is in appropriate state.
 *
 * It means that processing status is changed on:
 * <ul>
 * <li>paused - if configuration's processing is in execution state</li>
 * <li>running - if configuration's processing is in paused state</li>
 * </ul>
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public class EventBasedStatusHolder implements StatusHolder {

    private final Monitor monitor;
    private final Monitor.Guard pauseGuard;
    private final Monitor.Guard continueGuard;

    // TODO: Scraper#STATUS_READY should be default status, but currently
    // ScraperExecutionStartEvent is not handled here.
    private int status = Scraper.STATUS_RUNNING;

    /**
     * Default class constructor which creates {@link Monitor.Guard} instances
     * using given {@link Monitor}. Created {@link Monitor.Guard}s are
     * responsible for allowing to change status if configuration's processing
     * is in appropriate state.
     *
     * @param monitor
     *            reference to {@link Monitor} which is going to be used to
     *            create {@link Monitor.Guard}s; must not be {@code null}
     */
    @Inject
    public EventBasedStatusHolder(final Monitor monitor) {
        if (monitor == null) {
            throw new IllegalArgumentException("Monitor is mandatory.");
        }
        this.monitor = monitor;
        this.pauseGuard = new Monitor.Guard(monitor) {
            @Override
            public boolean isSatisfied() {
                return status == Scraper.STATUS_RUNNING;
            }
        };
        this.continueGuard = new Monitor.Guard(monitor) {

            @Override
            public boolean isSatisfied() {
                return status == Scraper.STATUS_PAUSED;
            }
        };
    }

    /**
     * Changes status from {@link Scraper#STATUS_RUNNING} to
     * {@link Scraper#STATUS_PAUSED}. This method is called when
     * {@link ScraperExecutionPausedEvent} has been fired.
     *
     * @param event
     *            an instance of {@link ScraperExecutionPausedEvent}
     */
    @Subscribe
    public void pause(final ScraperExecutionPausedEvent event)
            throws InterruptedException {
        try {
            monitor.enterWhen(pauseGuard);
            status = Scraper.STATUS_PAUSED;
        } finally {
            monitor.leave();
        }
    }

    /**
     * Changes status from {@link Scraper#STATUS_PAUSED} to
     * {@link Scraper#STATUS_RUNNING}. This method is called when
     * {@link ScraperExecutionContinuedEvent} has been fired.
     *
     * @param event
     *            an instance of {@link ScraperExecutionContinuedEvent}
     */
    @Subscribe
    public void resume(final ScraperExecutionContinuedEvent event)
            throws InterruptedException {
        try {
            monitor.enterWhen(continueGuard);
            status = Scraper.STATUS_RUNNING;
        } finally {
            monitor.leave();
        }
    }

    /**
     * Changes status to {@link Scraper#STATUS_STOPPED}. This method is called
     * when {@link ScraperExecutionStoppedEvent} has been fired.
     *
     * @param event
     *            an instance of {@link ScraperExecutionStoppedEvent}
     */
    @Subscribe
    public void stop(final ScraperExecutionStoppedEvent event) {
        status = Scraper.STATUS_STOPPED;
    }

    /**
     * Changes status to {@link Scraper#STATUS_EXIT}. This method is called when
     * {@link ScraperExecutionExitEvent} has been fired.
     *
     * @param event
     *            an instance of {@link ScraperExecutionExitEvent}
     */
    @Subscribe
    public void exit(final ScraperExecutionExitEvent event) {
        status = Scraper.STATUS_EXIT;
    }

    /**
     * {@inheritDoc}
     */
    public int getStatus() {
        return status;
    }

}
