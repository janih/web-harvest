package org.webharvest.ioc;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

// TODO Missing documentation
// TODO Missing unit test
public final class ScrapingInterceptor implements MethodInterceptor {

    private static final Logger LOG =
        LoggerFactory.getLogger(ScrapingInterceptor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Injector injector = InjectorHelper.getInjector();
        final ScraperScope scope = injector.getInstance(ScraperScope.class);
        scope.enter(injector.getInstance(AttributeHolder.class));
        LOG.info("Entering scraper scope [{}]", invocation.getThis());
        try {
            return invocation.proceed();
        } finally {
            LOG.info("Leaving scraper scope [{}]", invocation.getThis());
            scope.exit();
        }
    }

}
