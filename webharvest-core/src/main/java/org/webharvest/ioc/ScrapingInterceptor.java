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

// TODO Missing documentation
// TODO Missing unit test
public final class ScrapingInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory
            .getLogger(ScrapingInterceptor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        if (!(invocation.getThis() instanceof Harvester)) {
            throw new IllegalStateException("Not an instance of Harvester");
        }
        return invoke(InjectorHelper.getInjector(),
                (Harvester) invocation.getThis(), new Callback() {

            @Override
            public Object execute() throws Throwable {
                return invocation.proceed();
            }});
    }

    private Object invoke(final Injector injector, final Harvester harvester,
            final Callback callback) throws Throwable {
        final ScraperScope scope = injector.getInstance(ScraperScope.class);
        final ScrapingAwareHelper helper =
                injector.getInstance(ScrapingAwareHelper.class);
        scope.enter(injector.getInstance(AttributeHolder.class));
        helper.onBeforeScraping(harvester);
        try {
            return callback.execute();
        } finally {
            helper.onAfterScraping(harvester);
            scope.exit();
        }
    }

    // TODO Missing documentation
    interface Callback {

        // TODO Missing documentation
        Object execute() throws Throwable;

    }

    // TODO Missing documentation
    // TODO Missing unit test
    @Singleton
    public static final class ScrapingAwareHelper {

        private final List<ScrapingAware> listeners =
                new ArrayList<ScrapingAware>();

        // TODO Missing documentation
        // TODO Missing unit test
        // FIXME rbala Not thread safe
        // TODO Synchronize listener's registration
        public void addListener(final ScrapingAware listener) {
            listeners.add(listener);
        }

        // TODO Missing documentation
        // TODO Missing unit test
        public void onBeforeScraping(final Harvester harvester) {
            LOG.debug("Entering scraper scope [{}]", harvester);
            for (final ScrapingAware listener : listeners) {
                listener.onBeforeScraping(harvester);
            }
        }

        // TODO Missing documentation
        // TODO Missing unit test
        public void onAfterScraping(final Harvester harvester) {
            LOG.debug("Leaving scraper scope [{}]", harvester);
            for (final ScrapingAware listener : listeners) {
                listener.onAfterScraping(harvester);
            }
        }

    }

}
