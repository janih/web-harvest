package org.webharvest.ioc;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.Harvester;
import org.webharvest.ScrapingAware;

import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Guice AOP interceptor responsible for taking action for method annotated with
 * {@link Scraping} annotation. New scraping scope is created for each annotated
 * method and is persistent until the end of invocation. Only ancestors of
 * {@link Harvester} are applicable. An attempt to apply scraping scope to any
 * other type will end up with thrown {@link IllegalStateException}. Just after
 * the scope is created and shortly before it is left all registered
 * {@link ScrapingAware} listeners are notified about the lifecycle change.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Scraping
 * @see MethodInterceptor
 * @see ScrapingAware
 */
public final class ScrapingInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory
            .getLogger(ScrapingInterceptor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Object subject = invocation.getThis();
        if (!(subject instanceof Harvester)) {
            throw new IllegalStateException("Not an instance of Harvester");
        }
        return invoke(InjectorHelper.getInjector(),
                (Harvester) subject, new Callback() {

                    @Override
                    public Object execute() throws Throwable {
                        return invocation.proceed();
                    }
                });
    }

    /**
     * Helper method that creates new scraping scope, executes one of the
     * {@link Harvester} object's method (the one annotated with
     * {@code Scraping} annotation) and leaves it. Just after the scope is
     * created and shortly before it is left all registered
     * {@link ScrapingAware} listeners are notified about the lifecycle change.
     */
    // FIXME rbala Since we use injector directly then we can not test scope management on unit level!
    private Object invoke(final Injector injector, final Harvester harvester,
            final Callback callback) throws Throwable {
        final ScraperScope scope = injector.getInstance(ScraperScope.class);
        final ScrapingAwareHelper helper = injector
                .getInstance(ScrapingAwareHelper.class);
        scope.enter(injector.getInstance(AttributeHolder.class));
        helper.onBeforeScraping(harvester);
        try {
            return callback.execute();
        } finally {
            helper.onAfterScraping(harvester);
            scope.exit();
        }
    }

    /**
     * Represents method invocation helper that allows to delay execution of
     * code to the last responsible moment and treat it just like any regular
     * parameter to any method.
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     */
    interface Callback {

        /**
         * Execute some code in delayed fashion.
         *
         * @return result of reflective method invocation.
         * @throws Throwable
         *             Occurred exception thrown upon execution.
         */
        Object execute() throws Throwable;

    }

    /**
     * Guice aware helper class that maintains collection of registered
     * {@link ScrapingAware} listeners. It simplifies notification of all
     * registered listeners about updates the instances of {@link ScrapingAware}
     * are interested in. That is when new scraping scope is created or tore
     * down. It has almost exactly the same methods (
     * {@link #onBeforeScraping(Harvester)} and
     * {@link #onAfterScraping(Harvester)} as those introduced by
     * {@link ScrapingAware}. However by contrast it does not implement this
     * interface as there is a threat it would be automatically self-registered
     * by Guice. The instance of this object is meant to be a singleton
     * (especially for Guice).
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     * @see ScrapingAware
     */
    // TODO Missing unit test
    // TODO It is easy to register listener but how about unregistering it?
    // TODO Resolve duplication of methods with ScrapingAware
    @Singleton
    public static final class ScrapingAwareHelper {

        /**
         * Collection of {@link ScrapingAware} listeners.
         */
        private final List<ScrapingAware> listeners =
                new ArrayList<ScrapingAware>();

        /**
         * Register {@link ScrapingAware} listener.
         *
         * @param listener
         *            reference to listener object.
         */
        // FIXME rbala Not thread safe
        // TODO Synchronize listener's registration
        public void addListener(final ScrapingAware listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener is required");
            }
            listeners.add(listener);
        }

        /**
         * Notify all registered listeners about new scraping scope.
         *
         * @param harvester
         *            reference to the {@link Harvester} the event applies to.
         */
        // TODO Missing unit test
        public void onBeforeScraping(final Harvester harvester) {
            LOG.debug("Entering scraper scope [{}]", harvester);
            for (final ScrapingAware listener : listeners) {
                listener.onBeforeScraping(harvester);
            }
        }

        /**
         * Notify all registered listeners about scraping scope that is about
         * the end.
         *
         * @param harvester
         *            reference to the {@link Harvester} the event applies to.
         */
        // TODO Missing unit test
        public void onAfterScraping(final Harvester harvester) {
            LOG.debug("Leaving scraper scope [{}]", harvester);
            for (final ScrapingAware listener : listeners) {
                listener.onAfterScraping(harvester);
            }
        }

    }

}
